package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.doesNotContain
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotInstanceOf
import assertk.assertions.isTrue
import com.soywiz.klock.DateTime
import com.soywiz.klock.hours
import me.saket.kgit.GitConfig
import me.saket.kgit.GitIdentity
import me.saket.kgit.PushResult
import me.saket.kgit.RealGit
import me.saket.kgit.SshPrivateKey
import me.saket.press.data.shared.Note
import me.saket.press.data.shared.NoteQueries
import me.saket.press.shared.BuildKonfig
import me.saket.press.shared.Platform
import me.saket.press.shared.PlatformHost.Android
import me.saket.press.shared.containsOnly
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.settings.FakeSetting
import me.saket.press.shared.sync.SyncState.IN_FLIGHT
import me.saket.press.shared.sync.SyncState.PENDING
import me.saket.press.shared.sync.SyncState.SYNCED
import me.saket.press.shared.sync.git.File
import me.saket.press.shared.sync.git.FileName
import me.saket.press.shared.sync.git.FileNameRegister
import me.saket.press.shared.sync.git.GitSyncer
import me.saket.press.shared.sync.git.GitSyncerConfig
import me.saket.press.shared.sync.git.UtcTimestamp
import me.saket.press.shared.sync.git.children
import me.saket.press.shared.sync.git.delete
import me.saket.press.shared.sync.git.relativePathIn
import me.saket.press.shared.sync.git.service.GitRepositoryInfo
import me.saket.press.shared.testDeviceInfo
import me.saket.press.shared.time.FakeClock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GitSyncerTest : BaseDatabaeTest() {

  private val deviceInfo = testDeviceInfo()
  private val noteQueries get() = database.noteQueries
  private val clock = FakeClock()
  private val config = GitSyncerConfig(
      remote = GitRepositoryInfo(
          owner = "ignored",
          name = "ignored",
          url = "ignored",
          sshUrl = BuildKonfig.GIT_TEST_REPO_SSH_URL,
          defaultBranch = BuildKonfig.GIT_TEST_REPO_BRANCH
      ),
      sshKey = SshPrivateKey(BuildKonfig.GIT_TEST_SSH_PRIV_KEY),
      user = GitIdentity(name = "Test syncer author", email = "test@test.com")
  )
  private val configSetting = FakeSetting(config)
  private val git = DelegatingGit(delegate = RealGit())
  private val syncer = GitSyncer(
      git = git,
      config = configSetting,
      database = database,
      deviceInfo = deviceInfo,
      clock = clock,
      lastSyncedAt = FakeSetting(null),
      lastPushedSha1 = FakeSetting(null)
  )

  private val expectUnSyncedNotes = mutableListOf<NoteId>()

  private fun canRunTests(): Boolean {
    // todo: make tests work for native platforms.
    return BuildKonfig.GIT_TEST_SSH_PRIV_KEY.isNotBlank() && Platform.host == Android
  }

  @BeforeTest
  fun setUp() {
    RemoteRepositoryRobot {
      deleteEverything()
    }
  }

  @AfterTest
  fun cleanUp() {
    deviceInfo.appStorage.delete(recursively = true)

    val unsyncedNotes = noteQueries.allNotes().executeAsList()
        .filter { it.syncState != SYNCED }
        .filterNot { it.id in expectUnSyncedNotes }

    if (unsyncedNotes.isNotEmpty()) {
      println("\nUNSYNCED NOTES FOUND: ")
      unsyncedNotes.forEach { println(it) }
      error("Unsynced notes found on completion of test")
    }
  }

  @Test fun `pull notes from a non-empty repo`() {
    if (!canRunTests()) return

    val firstCommitTime = clock.nowUtc() - 10.hours
    val secondCommitTime = clock.nowUtc()

    // Given: Remote repository has some notes over multiple commits.
    RemoteRepositoryRobot {
      commitFiles(
          message = "First commit",
          time = firstCommitTime,
          add = listOf(
              "note_1.md" to "# The Witcher",
              "note_2.md" to "# Uncharted: The Lost Legacy"
          )
      )
      commitFiles(
          message = "Second commit",
          time = secondCommitTime,
          add = listOf(
              "note_3.md" to "# Overcooked",
              "note_4.md" to "# The Last of Us"
          )
      )
      forcePush()
    }

    // Given: User hasn't saved any notes on this device yet.
    assertThat(noteQueries.visibleNotes().executeAsList()).isEmpty()

    syncer.sync()

    // Check that the notes were pulled and saved into DB.
    val notesAfterSync = noteQueries.visibleNotes().executeAsList()
    assertThat(notesAfterSync.map { it.content }).containsOnly(
        "# The Witcher",
        "# Uncharted: The Lost Legacy",
        "# Overcooked",
        "# The Last of Us"
    )

    notesAfterSync.first { it.content == "# The Witcher" }.apply {
      assertThat(createdAt).isEqualTo(firstCommitTime)
      assertThat(updatedAt).isEqualTo(firstCommitTime)
      assertThat(isArchived).isFalse()
      assertThat(isPendingDeletion).isFalse()
    }

    notesAfterSync.first { it.content == "# The Last of Us" }.apply {
      assertThat(createdAt).isEqualTo(secondCommitTime)
    }
  }

  @Test fun `push notes to an empty repo`() {
    if (!canRunTests()) return

    // Given: Remote repository is empty.
    val remote = RemoteRepositoryRobot()

    // Given: This device has some notes.
    noteQueries.testInsert(
        fakeNote(
            content = "# Nicolas Cage \nis a national treasure",
            clock = clock
        ),
        fakeNote(
            content = "# Witcher 3 \nKings Die, Realms Fall, But Magic Endures",
            clock = clock
        )
    )

    syncer.sync()

    // Check that the local note(s) were pushed to remote.
    assertThat(remote.fetchNoteFiles()).containsOnly(
        "nicolas_cage.md" to "# Nicolas Cage \nis a national treasure",
        "witcher_3.md" to "# Witcher 3 \nKings Die, Realms Fall, But Magic Endures"
    )
  }

  @Test fun `pull remote notes with same content but different filename`() {
    if (!canRunTests()) return

    RemoteRepositoryRobot {
      commitFiles(
          message = "Create test.md",
          time = clock.nowUtc(),
          add = listOf(
              "test.md" to "# Test",
              "test2.md" to "# Test"
          )
      )
      forcePush()
    }

    syncer.sync()

    val localNotes = noteQueries.visibleNotes().executeAsList().map { it.content }
    assertThat(localNotes).containsOnly(
        "# Test",
        "# Test"
    )
  }

  @Test fun `merge unique local and remote notes without conflicts`() {
    if (!canRunTests()) return

    // Given: Remote and local notes are saved in mixed order.
    val remoteTime1 = clock.nowUtc() - 10.hours
    val localTime1 = remoteTime1 + 2.hours
    val remoteTime2 = remoteTime1 + 4.hours
    val localTime2 = clock.nowUtc()

    // Given: Remote repository has some notes over multiple commits.
    val remote = RemoteRepositoryRobot {
      commitFiles(
          message = "First commit",
          time = remoteTime1,
          add = listOf("note_1.md" to "# Uncharted: The Lost Legacy")
      )
      commitFiles(
          message = "Second commit",
          time = remoteTime2,
          add = listOf("note_2.md" to "# The Last of Us")
      )
      forcePush()
    }

    // Given: This device has some notes.
    noteQueries.testInsert(
        fakeNote(
            content = "# Nicolas Cage \nis a national treasure",
            updatedAt = localTime1
        ),
        fakeNote(
            content = "# Witcher 3 \nKings Die, Realms Fall, But Magic Endures",
            updatedAt = localTime2
        )
    )

    println("\nNotes in local DB before sync: ")
    noteQueries.visibleNotes()
        .executeAsList()
        .sortedBy { it.updatedAt }
        .forEach { println("${it.id} ${it.content.replace("\n", " ")}") }

    syncer.sync()

    // Check: both local and remote have same notes with same timestamps.
    val localNotes = noteQueries.visibleNotes()
        .executeAsList()
        .sortedBy { it.updatedAt }

    assertThat(localNotes.map { it.content }).containsExactly(
        "# Uncharted: The Lost Legacy",
        "# Nicolas Cage \nis a national treasure",
        "# The Last of Us",
        "# Witcher 3 \nKings Die, Realms Fall, But Magic Endures"
    )

    assertThat(remote.fetchNoteFiles()).containsOnly(
        "note_1.md" to "# Uncharted: The Lost Legacy",
        "note_2.md" to "# The Last of Us",
        "nicolas_cage.md" to "# Nicolas Cage \nis a national treasure",
        "witcher_3.md" to "# Witcher 3 \nKings Die, Realms Fall, But Magic Endures"
    )
  }

  @Test fun `merging local and remote notes with the same ID without conflicts`() {
    if (!canRunTests()) return

    val note = fakeNote(content = "# Uncharted\nThe Lost Legacy")
    noteQueries.testInsert(note)

    RemoteRepositoryRobot {
      createRecord("uncharted.md", note.id)
      commitFiles(
          message = "Create 'uncharted.md",
          add = listOf("uncharted.md" to "# Uncharted\nThe Lost Legacy")
      )
      forcePush()
    }

    syncer.sync()

    val syncedNotes = noteQueries.allNotes().executeAsList()
    assertThat(syncedNotes).hasSize(1)
    assertThat(syncedNotes.map { it.id }).containsExactly(note.id)
  }

  @Test fun `merge local and remote notes with content conflict`() {
    if (!canRunTests()) return

    clock.rewindTimeBy(10.hours)

    // Given: a note was created on another device.
    val remote = RemoteRepositoryRobot {
      commitFiles(
          message = "First commit",
          time = clock.nowUtc(),
          add = listOf("uncharted.md" to "# Uncharted")
      )
      forcePush()
    }
    syncer.sync()

    // Given: the same note was edited locally.
    val locallyEditedNote = noteQueries.visibleNotes().executeAsOne()
    clock.advanceTimeBy(1.hours)
    noteQueries.updateContent(
        id = locallyEditedNote.id,
        content = "# Uncharted\nLocal edit",
        updatedAt = clock.nowUtc()
    )

    // Given: the same note was edited on remote in a conflicting way.
    clock.advanceTimeBy(1.hours)
    with(remote) {
      commitFiles(
          message = "Second commit",
          time = clock.nowUtc(),
          add = listOf("uncharted.md" to "# Uncharted\nRemote edit")
      )
      forcePush()
    }

    // The conflict should get auto-resolved here.
    syncer.sync()

    val localNotes = noteQueries.visibleNotes().executeAsList().sortedBy { it.updatedAt }

    // The local note should get duplicated as a new note and then
    // the local note should get overridden by the server copy.
    assertThat(localNotes).hasSize(2)
    assertThat(localNotes[0].content).isEqualTo("# Uncharted\nLocal edit")
    assertThat(localNotes[0].id).isNotEqualTo(locallyEditedNote.id)

    assertThat(localNotes[1].content).isEqualTo("# Uncharted\nRemote edit")
    assertThat(localNotes[1].id).isEqualTo(locallyEditedNote.id)
  }

  @Test fun `merge local and remote notes with delete conflict (without remote register)`() {
    if (!canRunTests()) return

    clock.rewindTimeBy(10.hours)

    // Given: a note was created on another device.
    val remote = RemoteRepositoryRobot {
      commitFiles(
          message = "Create 'uncharted.md'",
          time = clock.nowUtc(),
          add = listOf("uncharted.md" to "# Uncharted")
      )
      forcePush()
    }
    syncer.sync()

    // Given: the same note was renamed locally, effectively DELETING the old file.
    val locallyEditedNote = noteQueries.visibleNotes().executeAsOne()
    clock.advanceTimeBy(1.hours)
    noteQueries.updateContent(
        id = locallyEditedNote.id,
        content = "# Uncharted2\nLocal edit",
        updatedAt = clock.nowUtc()
    )

    // Given: the same note was edited on remote
    clock.advanceTimeBy(1.hours)
    with(remote) {
      pull()
      commitFiles(
          message = "Update 'uncharted.md'",
          time = clock.nowUtc(),
          add = listOf("uncharted.md" to "# Uncharted\nRemote edit")
      )
      forcePush()
    }

    // The conflict should get auto-resolved here.
    syncer.sync()

    val localNotes = noteQueries.visibleNotes().executeAsList().sortedBy { it.updatedAt }

    println("localNotes: ")
    localNotes.forEach { println(it) }

    // The local note should get duplicated as a new note and then
    // the local note should get overridden by the server copy.
    assertThat(localNotes).hasSize(2)
    assertThat(localNotes[0].content).isEqualTo("# Uncharted2\nLocal edit")
    assertThat(localNotes[0].id).isNotEqualTo(locallyEditedNote.id)

    assertThat(localNotes[1].content).isEqualTo("# Uncharted\nRemote edit")
    assertThat(localNotes[1].id).isEqualTo(locallyEditedNote.id)
  }

  @Test fun `merge local and remote notes with delete conflict (with remote register)`() {
    if (!canRunTests()) return

    clock.rewindTimeBy(10.hours)

    // Given: a note was created on another device.
    val remoteNoteId = NoteId.from("ebc77dbc-9201-4902-bd4b-2ce8a99059a7")
    val remote = RemoteRepositoryRobot {
      createRecord("uncharted.md", id = remoteNoteId)
      commitFiles(
          message = "Create 'uncharted.md'",
          time = clock.nowUtc(),
          add = listOf("uncharted.md" to "# Uncharted")
      )
      forcePush()
    }
    syncer.sync()

    // Given: the same note was renamed locally, effectively DELETING the old file.
    clock.advanceTimeBy(1.hours)
    noteQueries.updateContent(
        id = remoteNoteId,
        content = "# Uncharted2\nLocal edit",
        updatedAt = clock.nowUtc()
    )

    // Given: the same note was edited on remote
    clock.advanceTimeBy(1.hours)
    with(remote) {
      pull()
      commitFiles(
          message = "Update 'uncharted.md'",
          time = clock.nowUtc(),
          add = listOf("uncharted.md" to "# Uncharted\nRemote edit")
      )
      forcePush()
    }

    // The conflict should get auto-resolved here.
    syncer.sync()

    val localNotes = noteQueries.visibleNotes().executeAsList().sortedBy { it.updatedAt }

    println("localNotes: ")
    localNotes.forEach { println(it) }

    // The local note should get duplicated as a new note and then
    // the local note should get overridden by the server copy.
    assertThat(localNotes).hasSize(2)
    assertThat(localNotes[0].content).isEqualTo("# Uncharted2\nLocal edit")
    assertThat(localNotes[0].id).isNotEqualTo(remoteNoteId)

    assertThat(localNotes[1].content).isEqualTo("# Uncharted\nRemote edit")
    assertThat(localNotes[1].id).isEqualTo(remoteNoteId)
  }

  // TODO
  @Test fun `merge local and remote notes with filename and content conflict`() {
    if (!canRunTests()) return

    // setup:
    //  notePath in pulledPathsToDiff
    //  same file content
    //  oldPath also in pulledPathsToDiff
  }

  @Test fun `notes created locally with the same headings are stored in separate files`() {
    if (!canRunTests()) return

    val now = clock.nowUtc()
    noteQueries.testInsert(
        fakeNote(updatedAt = now + 1.hours, content = "# Shopping List\nMangoes and strawberries"),
        fakeNote(updatedAt = now + 2.hours, content = "# Shopping List\nMilk and eggs"),
        fakeNote(updatedAt = now + 3.hours, content = "Note without heading"),
        fakeNote(updatedAt = now + 4.hours, content = "Another note without heading")
    )
    syncer.sync()

    val remoteFiles = RemoteRepositoryRobot().fetchNoteFiles()
    assertThat(remoteFiles).containsOnly(
        "shopping_list.md" to "# Shopping List\nMangoes and strawberries",
        "shopping_list_2.md" to "# Shopping List\nMilk and eggs",
        "untitled_note.md" to "Note without heading",
        "untitled_note_2.md" to "Another note without heading"
    )
  }

  @Test fun `notes created on local and remote with the same headings are stored in separate files`() {
    if (!canRunTests()) return

    val note = fakeNote("# Shopping List\n(local)", isArchived = true)
    noteQueries.testInsert(note)
    syncer.sync()

    val remote = RemoteRepositoryRobot {
      pull()
      commitFiles(
          message = "Create new shopping list",
          add = listOf("shopping_list.md" to "# Shopping List\n(remote)")
      )
      forcePush()
    }
    noteQueries.setArchived(id = note.id, isArchived = false, updatedAt = clock.nowUtc())
    syncer.sync()

    val localNotes = noteQueries.allNotes().executeAsList()
    assertThat(localNotes).hasSize(2)

    assertThat(remote.fetchNoteFiles()).containsOnly(
        "shopping_list.md" to "# Shopping List\n(remote)",
        "shopping_list_2.md" to "# Shopping List\n(local)"
    )
  }

  @Test fun `sync notes deleted on remote`() {
    if (!canRunTests()) return

    noteQueries.insert(
        id = NoteId.generate(),
        content = "# Horizon Zero Dawn",
        createdAt = clock.nowUtc(),
        updatedAt = clock.nowUtc()
    )
    syncer.sync()

    val savedNotes = { noteQueries.allNotes().executeAsList() }
    assertThat(savedNotes()).isNotEmpty()

    clock.advanceTimeBy(2.hours)
    RemoteRepositoryRobot {
      pull()
      commitFiles(
          message = "Delete notes",
          time = clock.nowUtc(),
          delete = listOf("horizon_zero_dawn.md")
      )
      forcePush()
    }
    syncer.sync()

    assertThat(savedNotes()).isEmpty()
  }

  // TODO
  @Test fun `sync notes deleted locally`() {
    if (!canRunTests()) return
  }

  // TODO
  @Test fun `filename-register from remote is used for determining note ID`() {
    if (!canRunTests()) return
  }

  @Test fun `ignore notes that are already synced`() {
    if (!canRunTests()) return

    noteQueries.testInsert(
        fakeNote(content = "# The Last of Us II", syncState = SYNCED, clock = clock),
        fakeNote(content = "# Horizon Zero Dawn", syncState = PENDING, clock = clock)
    )
    syncer.sync()

    val remoteFiles = RemoteRepositoryRobot().fetchNoteFiles()
    assertThat(remoteFiles).containsOnly(
        "horizon_zero_dawn.md" to "# Horizon Zero Dawn"
    )
  }

  @Test fun `sync notes with renamed heading`() {
    if (!canRunTests()) return

    val noteId = NoteId.generate()
    noteQueries.testInsert(
        fakeNote(
            id = noteId,
            content = """
            |# John
            |I'm thinking I'm back
            """.trimMargin()
        )
    )
    syncer.sync()

    val remote = RemoteRepositoryRobot()
    assertThat(remote.fetchNoteFiles()).containsOnly(
        "john.md" to "# John\nI'm thinking I'm back"
    )

    noteQueries.updateContent(
        id = noteId,
        updatedAt = clock.nowUtc(),
        content =
        """
        |# John Wick
        |I'm thinking I'm back
        """.trimMargin()
    )
    syncer.sync()

    assertThat(remote.fetchNoteFiles()).containsOnly(
        "john_wick.md" to "# John Wick\nI'm thinking I'm back"
    )
  }

  @Test fun `sync note that was archived locally`() {
    if (!canRunTests()) return

    val note1 = fakeNote("# Horizon Zero Dawn", isArchived = true)
    val note2 = fakeNote("# Uncharted", isArchived = false)
    noteQueries.testInsert(note1, note2)
    syncer.sync()

    noteQueries.setArchived(
        id = note2.id,
        isArchived = true,
        updatedAt = clock.nowUtc()
    )
    syncer.sync()

    val notes = noteQueries.allNotes().executeAsList()
    assertThat(notes.all { it.isArchived }).isTrue()

    assertThat(RemoteRepositoryRobot().fetchNoteFiles()).containsOnly(
        "archived/horizon_zero_dawn.md" to "# Horizon Zero Dawn",
        "archived/uncharted.md" to "# Uncharted"
    )
  }

  @Test fun `sync notes that were archived on both local and remote`() {
    if (!canRunTests()) return

    val note1 = fakeNote("# Horizon Zero Dawn")
    val note2 = fakeNote("# Uncharted")
    noteQueries.testInsert(note1, note2)
    syncer.sync()

    // Archive both notes: one on local and the other on remote.
    noteQueries.setArchived(id = note1.id, isArchived = true, updatedAt = clock.nowUtc())
    val remote = RemoteRepositoryRobot {
      pull()
      commitFiles(
          message = "Archive uncharted.md",
          rename = listOf(
              "uncharted.md" to "archived/uncharted.md"
          )
      )
      forcePush()
    }
    syncer.sync()

    val notes = noteQueries.allNotes().executeAsList()
    println("Local notes after syncing:"); notes.forEach { println(it) }
    assertThat(notes.all { it.isArchived }).isTrue()

    assertThat(remote.fetchNoteFiles()).containsOnly(
        "archived/horizon_zero_dawn.md" to "# Horizon Zero Dawn",
        "archived/uncharted.md" to "# Uncharted"
    )
  }

  @Test fun `sync note archived on remote`() {
    if (!canRunTests()) return

    RemoteRepositoryRobot {
      commitFiles(
          message = "Archive uncharted.md",
          add = listOf("archived/uncharted.md" to "# Uncharted")
      )
      forcePush()
    }
    syncer.sync()
    assertThat(noteQueries.allNotes().executeAsOne().isArchived).isTrue()
  }

  @Test fun `sync unarchived note`() {
    if (!canRunTests()) return

    val remote = RemoteRepositoryRobot {
      commitFiles(
          message = "Archive uncharted.md",
          add = listOf("archived/uncharted.md" to "# Uncharted")
      )
      forcePush()
    }
    syncer.sync()

    val note = { noteQueries.allNotes().executeAsOne() }
    noteQueries.setArchived(
        id = note().id,
        isArchived = false,
        updatedAt = clock.nowUtc()
    )
    syncer.sync()

    assertThat(note().isArchived).isFalse()
    assertThat(remote.fetchNoteFiles()).containsOnly("uncharted.md" to "# Uncharted")
  }

  @Test fun `file renames are followed correctly`() {
    if (!canRunTests()) return

    noteQueries.insert(
        id = NoteId.generate(),
        content = "# Horizon Zero Dawn",
        createdAt = clock.nowUtc(),
        updatedAt = clock.nowUtc()
    )
    syncer.sync()

    // kgit should be able to identify an ADD + DELETE as a RENAME.
    RemoteRepositoryRobot {
      pull()
      commitFiles(
          message = "Delete notes",
          time = clock.nowUtc(),
          delete = listOf("horizon_zero_dawn.md"),
          add = listOf("archived/horizon_zero_dawn.md" to "# Horizon Zero Dawn")
      )
      forcePush()
    }
    syncer.sync()

    val savedNote = noteQueries.allNotes().executeAsOne()
    assertThat(savedNote.isArchived).isTrue()
  }

  @Test fun `notes are re-synced when syncing is re-enabled`() {
    if (!canRunTests()) return

    val note = fakeNote(content = "# Potter\nYou're a wizard Harry", clock = clock)
    noteQueries.testInsert(note)

    RemoteRepositoryRobot().let { remote1 ->
      syncer.sync()
      assertThat(remote1.fetchNoteFiles()).containsOnly("potter.md" to "# Potter\nYou're a wizard Harry")
      remote1.deleteEverything()
    }

    syncer.disable()
    assertThat(deviceInfo.appStorage.children()).isEmpty()

    RemoteRepositoryRobot().let { remote2 ->
      assertThat(remote2.fetchNoteFiles()).isEmpty()

      configSetting.set(config)
      syncer.sync()
      assertThat(remote2.fetchNoteFiles()).containsOnly("potter.md" to "# Potter\nYou're a wizard Harry")
    }
  }

  // TODO
  @Test fun `notes are re-synced if syncing fails with an unhandled error`() {
    if (!canRunTests()) return
  }

  // TODO
  @Test fun `clear staging area if syncing fails with an unhandled error`() {
    if (!canRunTests()) return
  }

  @Test fun `notes stay in-flight if sync fails unexpectedly`() {
    if (!canRunTests()) return

    val remote = RemoteRepositoryRobot {
      commitFiles(
          message = "Create 'nicolas.md'",
          add = listOf("nicolas.md" to "# Nicolas")
      )
      forcePush()
    }
    syncer.sync()

    val newNote = fakeNote("# Batman")
    noteQueries.testInsert(newNote)

    // Syncs fail in different ways.
    git.onPull = { error("You just couldn't let me go, could you?") }
    syncer.sync()

    git.onPull = null
    git.pushResult = PushResult.Failure(reason = "Joker")
    syncer.sync()

    // Verify: the new note doesn't get mark as synced.
    assertThat(remote.fetchNoteFiles()).containsOnly("nicolas.md" to "# Nicolas")
    assertThat(noteQueries.note(newNote.id).executeAsOne().syncState).isEqualTo(IN_FLIGHT)
    expectUnSyncedNotes += newNote.id
  }

  @Test fun `reset all dirty state to last synced sha1 on start`() {
    if (!canRunTests()) return

    // First sync goes through fine.
    val note1 = fakeNote("# Batman 1")
    noteQueries.testInsert(note1)
    syncer.sync()

    val unrelatedFile = File(syncer.directory, "unrelated_dirty_file.md")
    unrelatedFile.write("Any random file that wasn't created from a note.")

    // Second sync fails.
    val note2 = fakeNote("# Batman 2")
    noteQueries.testInsert(note2)
    git.pushResult = PushResult.Failure(reason = "Two-Face")
    syncer.sync()

    assertThat(unrelatedFile.exists).isFalse()
    val remoteFiles = RemoteRepositoryRobot().fetchNoteFiles().map { (path) -> path }
    assertThat(remoteFiles).doesNotContain("unrelated_dirty_file.md")
    assertThat(remoteFiles).containsOnly("batman_1.md")

    val noteFiles = {
      syncer.directory
          .children()
          .filter { it.path.endsWith(".md") }
    }

    // Both synced and unsynced notes will be present in the file directory right now.
    val notePaths = { noteFiles().map { it.relativePathIn(syncer.directory) } }
    assertThat(notePaths()).containsOnly("batman_1.md", "batman_2.md")

    // The unsynced note will be in a dangling sync
    // state. It should get picked up on the next sync.
    assertThat(noteQueries.note(note2.id).executeAsOne().syncState).isEqualTo(IN_FLIGHT)

    // When sync is started again, it should
    // delete all unsynced files before starting.
    git.onPull = { error("don't need this to finish") }
    syncer.sync()
    expectUnSyncedNotes += note2.id

    assertThat(notePaths()).containsOnly("batman_1.md")
  }

  @Test fun `pickup all unsynced notes on start`() {
    noteQueries.testInsert(
        fakeNote("# The Dark Knight", syncState = PENDING),
        fakeNote("# The Dark Knight Rises", syncState = IN_FLIGHT)
    )

    syncer.sync()

    assertThat(RemoteRepositoryRobot().fetchNoteFiles()).containsOnly(
        "the_dark_knight.md" to "# The Dark Knight",
        "the_dark_knight_rises.md" to "# The Dark Knight Rises"
    )
    val unsyncedNotes = noteQueries.notesInState(listOf(PENDING, IN_FLIGHT)).executeAsList()
    assertThat(unsyncedNotes).isEmpty()
  }

  @Test fun `skip pushing if nothing was pulled or committed`() {
    if (!canRunTests()) return

    syncer.sync()
    assertThat(git.pushCount).isEqualTo(0)
  }

  @Test fun `skip pushing if nothing was committed`() {
    if (!canRunTests()) return

    RemoteRepositoryRobot {
      commitFiles(
          message = "Create 'nicolas.md'",
          add = listOf("nicolas.md" to "# Nicolas")
      )
      forcePush()
    }
    syncer.sync()
    assertThat(git.pushCount).isEqualTo(0)
  }

  private inner class RemoteRepositoryRobot(prepare: RemoteRepositoryRobot.() -> Unit = {}) {
    private val directory = File(deviceInfo.appStorage, "temp").apply { makeDirectory() }
    private val register = FileNameRegister(directory)
    private val gitRepo = RealGit().repository(
        path = directory.path,
        sshKey = config.sshKey,
        remoteSshUrl = config.remote.sshUrl,
        userConfig = GitConfig(
            "author" to listOf("name" to "Test remote author", "email" to "press@saket.me"),
            "committer" to listOf("name" to "Test remote committer", "email" to "")
        )
    )

    init {
      gitRepo.commitAll("Initial commit", timestamp = UtcTimestamp(clock), allowEmpty = true)
      gitRepo.checkout(config.remote.defaultBranch)
      prepare()
    }

    fun pull() {
      gitRepo.pull(rebase = true)
    }

    fun forcePush() {
      assertThat(gitRepo.push(force = true)).isNotInstanceOf(PushResult.Failure::class)
    }

    fun createRecord(notePath: String, id: NoteId) {
      register.createNewRecordFor(File(directory, notePath), id)
    }

    fun commitFiles(
      message: String,
      time: DateTime = clock.nowUtc(),
      add: List<Pair<FileName, FileName>> = emptyList(),
      delete: List<FileName> = emptyList(),
      rename: List<Pair<FileName, String>> = emptyList()
    ) {
      add.forEach { (name, body) ->
        File(directory, name).apply {
          parent!!.makeDirectory(recursively = true)
          write(body)
        }
      }
      delete.forEach { path ->
        File(directory, path).delete()
      }
      rename.forEach { (oldPath, newPath) ->
        File(directory, oldPath).renameTo(File(directory, newPath))
      }
      gitRepo.commitAll(message, timestamp = UtcTimestamp(time), allowEmpty = true)
    }

    /**
     * @return Pair(relative file path, file content).
     */
    fun fetchNoteFiles(): List<Pair<String, String>> {
      gitRepo.pull(rebase = true)
      return directory
          .children(recursively = true)
          .filter { it.extension == "md" && !it.relativePathIn(directory).startsWith(".") }
          .map { it.relativePathIn(directory) to it.read() }
    }

    fun deleteEverything() {
      directory.children().filter { it.name != ".git" }.forEach { it.delete(recursively = true) }
      commitFiles(message = "Emptiness", add = emptyList())
      forcePush()
      directory.delete(recursively = true)
    }
  }
}

private fun NoteQueries.testInsert(vararg notes: Note) {
  notes.forEach { testInsert(it) }
}
