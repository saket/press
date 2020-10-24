package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsExactly
import assertk.assertions.doesNotContain
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import co.touchlab.stately.concurrency.AtomicBoolean
import co.touchlab.stately.concurrency.value
import com.badoo.reaktive.test.observable.test
import com.soywiz.klock.DateTime
import com.soywiz.klock.hours
import com.soywiz.klock.seconds
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
import me.saket.press.shared.fakedata.fakeRepository
import me.saket.press.shared.localization.ENGLISH_STRINGS
import me.saket.press.shared.sync.SyncState.IN_FLIGHT
import me.saket.press.shared.sync.SyncState.PENDING
import me.saket.press.shared.sync.SyncState.SYNCED
import me.saket.press.shared.sync.git.File
import me.saket.press.shared.sync.git.FileName
import me.saket.press.shared.sync.git.FileNameRegister
import me.saket.press.shared.sync.git.GitRemoteAndAuth
import me.saket.press.shared.sync.git.GitSyncer
import me.saket.press.shared.sync.git.UtcTimestamp
import me.saket.press.shared.sync.git.children
import me.saket.press.shared.sync.git.delete
import me.saket.press.shared.sync.git.relativePathIn
import me.saket.press.shared.testDeviceInfo
import me.saket.press.shared.time.FakeClock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GitSyncerTest : BaseDatabaeTest() {
  override val database = DelegatingPressDatabase(super.database)
  private val noteQueries get() = database.noteQueries
  private val configQueries get() = database.folderSyncConfigQueries

  private val deviceInfo = testDeviceInfo()
  private val clock = FakeClock()
  private val remoteAndAuth = GitRemoteAndAuth(
    remote = fakeRepository().copy(
      sshUrl = BuildKonfig.GIT_TEST_REPO_SSH_URL,
      defaultBranch = BuildKonfig.GIT_TEST_REPO_BRANCH
    ),
    sshKey = SshPrivateKey(BuildKonfig.GIT_TEST_SSH_PRIV_KEY),
    user = GitIdentity(name = "Test syncer author", email = "test@test.com")
  )
  private val mergeConflicts = SyncMergeConflicts()
  private val backupBeforeFirstSync = AtomicBoolean(false)
  private val git = DelegatingGit(delegate = RealGit())
  private val syncer = GitSyncer(
    git = git,
    database = database,
    deviceInfo = deviceInfo,
    clock = clock,
    strings = ENGLISH_STRINGS,
    mergeConflicts = mergeConflicts,
    backupBeforeFirstSync = backupBeforeFirstSync
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
    configQueries.save(remote = remoteAndAuth)

    preventCommitsBeforePullsOrAfterPushes()
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
        message = "Create 'uncharted.md'",
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
        message = "Update 'uncharted.md'",
        time = clock.nowUtc(),
        add = listOf("uncharted.md" to "# Uncharted\nRemote edit")
      )
      forcePush()
    }

    // The conflict should get auto-resolved here.
    syncer.sync()

    val localNotes = noteQueries.visibleNotes().executeAsList().sortedBy { it.updatedAt }

    // The local note should get duplicated as a new note and then
    // the old local note should get overridden by the server copy.
    assertThat(localNotes).hasSize(2)
    assertThat(localNotes[0].content).isEqualTo("# Conflicted: Uncharted\nLocal edit")
    assertThat(localNotes[0].id).isNotEqualTo(locallyEditedNote.id)

    assertThat(localNotes[1].content).isEqualTo("# Uncharted\nRemote edit")
    assertThat(localNotes[1].id).isEqualTo(locallyEditedNote.id)
  }

  @Test fun `merge renamed local and remote notes (without remote register)`() {
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
    // the old local note should get overridden by the server copy.
    assertThat(localNotes).hasSize(2)
    assertThat(localNotes[0].content).isEqualTo("# Conflicted: Uncharted2\nLocal edit")
    assertThat(localNotes[0].id).isNotEqualTo(locallyEditedNote.id)

    assertThat(localNotes[1].content).isEqualTo("# Uncharted\nRemote edit")
    assertThat(localNotes[1].id).isEqualTo(locallyEditedNote.id)
  }

  @Test fun `merge renamed local and remote notes (with remote register)`() {
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
    // the old local note should get overridden by the server copy.
    assertThat(localNotes).hasSize(2)
    assertThat(localNotes[0].content).isEqualTo("# Conflicted: Uncharted2\nLocal edit")
    assertThat(localNotes[0].id).isNotEqualTo(remoteNoteId)

    assertThat(localNotes[1].content).isEqualTo("# Uncharted\nRemote edit")
    assertThat(localNotes[1].id).isEqualTo(remoteNoteId)
  }

  @Test fun `resolve merge by comparing timestamps on first sync`() {
    if (!canRunTests()) return
    clock.rewindTimeBy(10.hours)

    val remoteNoteId = NoteId.from("ebc77dbc-9201-4902-bd4b-2ce8a99059a7")
    RemoteRepositoryRobot {
      createRecord("uncharted.md", id = remoteNoteId)
      commitFiles(
        message = "Create 'uncharted.md'",
        time = clock.nowUtc(),
        add = listOf("uncharted.md" to "# Uncharted")
      )
      forcePush()
    }

    clock.advanceTimeBy(1.hours)

    val note = fakeNote("# Uncharted\nThe Lost Legacy", id = remoteNoteId)
    noteQueries.testInsert(note)

    syncer.sync()

    val localNotes = noteQueries.visibleNotes().executeAsList()
    assertThat(localNotes).hasSize(1)
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

    val noteToDelete = fakeNote("# Uncharted")
    noteQueries.testInsert(noteToDelete)
    syncer.sync()

    RemoteRepositoryRobot {
      pull()
      commitFiles(
        message = "Delete 'uncharted.md'",
        delete = listOf("uncharted.md")
      )
      commitFiles(
        message = "Add 'horizon_zero_dawn.md'",
        add = listOf("horizon_zero_dawn.md" to "# Horizon Zero Dawn")
      )
      forcePush()
    }
    syncer.sync()

    assertThat(noteQueries.note(noteToDelete.id).executeAsOneOrNull()).isNull()
  }

  @Test fun `deletion and creation of notes with the same heading on different devices`() {
    if (!canRunTests()) return

    val noteToDelete = fakeNote("# Uncharted")
    noteQueries.testInsert(noteToDelete)
    syncer.sync()

    noteQueries.markAsPendingDeletion(noteToDelete.id)
    RemoteRepositoryRobot {
      pull()
      commitFiles(
        message = "Create 'uncharted.md' on remote",
        add = listOf("uncharted.md" to "# Uncharted")
      )
      forcePush()
    }
    syncer.sync()

    // The pulled note will be saved before the local note is deleted.
    // This will unfortunately result in the remote note effectively get skipped.
    assertThat(noteQueries.allNotes().executeAsList()).isEmpty()
  }

  @Test fun `sync notes deleted locally`() {
    if (!canRunTests()) return

    val noteToDelete = fakeNote("# Uncharted")
    noteQueries.testInsert(noteToDelete)
    syncer.sync()

    val remote = RemoteRepositoryRobot()
    assertThat(remote.fetchNoteFiles()).containsOnly("uncharted.md" to "# Uncharted")

    noteQueries.updateContent(
      id = noteToDelete.id,
      content = "# Uncharted 4\nA Thief's End",
      updatedAt = clock.nowUtc()
    )
    noteQueries.markAsPendingDeletion(noteToDelete.id)

    val commitMessages = mutableListOf<String>()
    git.preCommits += { commitMessages += it }
    syncer.sync()

    // Note's updated content should be saved before it's deleted, and in separate commits.
    // Checking commit messages isn't a great way of testing, but I can't think of any other way.
    assertThat(commitMessages).containsAll(
      "Rename 'uncharted.md' → 'uncharted_4.md'",
      "Update 'uncharted_4.md'"
    )

    assertThat(noteQueries.allNotes().executeAsList()).isEmpty()
    assertThat(remote.fetchNoteFiles()).isEmpty()

    // The meta-records of the note should get deleted as well. Check that
    // its ID doesn't get reused for another note with the same content.
    remote.run {
      pull()
      commitFiles(
        message = "Add new 'uncharted.md'",
        add = listOf("uncharted.md" to "# Uncharted 4\nA Thief's End")
      )
      forcePush()
    }

    syncer.sync()
    assertThat(noteQueries.note(noteToDelete.id).executeAsOneOrNull()).isNull()
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
    val note2 = fakeNote("# Uncharted", isArchived = true)
    val note3 = fakeNote("# Uncharted", isArchived = false)
    noteQueries.testInsert(note1, note2, note3)
    syncer.sync()

    noteQueries.setArchived(
      id = note3.id,
      isArchived = true,
      updatedAt = clock.nowUtc()
    )
    syncer.sync()

    val notes = noteQueries.allNotes().executeAsList()
    assertThat(notes.all { it.isArchived }).isTrue()

    assertThat(RemoteRepositoryRobot().fetchNoteFiles()).containsOnly(
      "archived/horizon_zero_dawn.md" to "# Horizon Zero Dawn",
      "archived/uncharted.md" to "# Uncharted",
      "archived/uncharted_2.md" to "# Uncharted"
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

    val note1 = fakeNote("# Horizon Zero Dawn")
    val note2 = fakeNote("# ")
    noteQueries.testInsert(note1, note2)
    syncer.sync()

    // kgit should be able to identify these ADD + DELETE as RENAME.
    noteQueries.updateContent(id = note2.id, content = "", updatedAt = clock.nowUtc())
    noteQueries.setArchived(id = note2.id, isArchived = true, updatedAt = clock.nowUtc())
    RemoteRepositoryRobot {
      pull()
      commitFiles(
        message = "Archive horizon_zero_dawn.md",
        delete = listOf("horizon_zero_dawn.md"),
        add = listOf("archived/horizon_zero_dawn.md" to "# Horizon Zero Dawn")
      )
      forcePush()
    }
    syncer.sync()

    val savedNotes = noteQueries.allNotes().executeAsList()
    assertThat(savedNotes.all { it.isArchived }).isTrue()
    assertThat(savedNotes.map { it.id }).containsOnly(note1.id, note2.id)
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

      configQueries.save(remote = remoteAndAuth)
      syncer.sync()
      assertThat(remote2.fetchNoteFiles()).containsOnly("potter.md" to "# Potter\nYou're a wizard Harry")
    }
  }

  // TODO
  @Test fun `notes are re-synced if syncing fails with an unhandled error`() {
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
    val prePull = { error("You just couldn't let me go, could you?") }
    git.prePulls += prePull
    syncer.sync()

    git.prePulls -= prePull
    git.prePushes += { error("Joker") }
    syncer.sync()

    // Verify: the new note doesn't get mark as synced.
    assertThat(remote.fetchNoteFiles()).containsOnly("nicolas.md" to "# Nicolas")
    assertThat(noteQueries.note(newNote.id).executeAsOne().syncState).isEqualTo(IN_FLIGHT)
    expectUnSyncedNotes += newNote.id
  }

  @Test fun `reset all dirty state to last synced sha1 on start`() {
    if (!canRunTests()) return

    // First sync goes through fine.
    val remote = RemoteRepositoryRobot {
      commitFiles(
        message = "Create 'batman_1.md'",
        add = listOf("batman_1.md" to "# Batman 1")
      )
      forcePush()
    }
    syncer.sync()

    val unrelatedFile = File(syncer.directory, "unrelated_dirty_file.md")
    unrelatedFile.write("Any random file that wasn't created from a note.")

    // Second sync fails.
    val note2 = fakeNote("# Batman 2")
    val note3 = fakeNote("# Batman 3", isPendingDeletion = true)
    noteQueries.testInsert(note2, note3)
    git.prePushes += { error("Two-Face") }
    syncer.sync()

    assertThat(unrelatedFile.exists).isFalse()
    val remoteFiles = remote.fetchNoteFiles().map { (path) -> path }
    assertThat(remoteFiles).containsOnly("batman_1.md")
    assertThat(remoteFiles).doesNotContain("unrelated_dirty_file.md")

    // Any pending-deletion notes shouldn't get deleted yet.
    assertThat(noteQueries.note(note3.id).executeAsOneOrNull()).isNotNull()

    val noteFiles = {
      syncer.directory
        .children(recursively = true)
        .filter {
          val path = it.relativePathIn(syncer.directory)
          path.endsWith(".md") && !path.startsWith(".")
        }
    }

    // Both synced and unsynced notes will be present in the file directory right now.
    val notePaths = { noteFiles().map { it.relativePathIn(syncer.directory) } }
    assertThat(notePaths()).containsOnly("batman_1.md", "batman_2.md")

    // The unsynced note will be in a dangling sync
    // state. It should get picked up on the next sync.
    assertThat(noteQueries.note(note2.id).executeAsOne().syncState).isEqualTo(IN_FLIGHT)

    // When sync is started again, it should
    // delete all unsynced files before starting.
    git.prePulls += { error("don't need this to finish") }
    syncer.sync()
    expectUnSyncedNotes += listOf(note2.id, note3.id)

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
      createRecord("nicolas.md", id = NoteId.generate())
      commitFiles(
        message = "Create 'nicolas.md'",
        add = listOf("nicolas.md" to "# Nicolas")
      )
      forcePush()
    }
    syncer.sync()
    assertThat(git.pushCount).isEqualTo(0)
  }

  @Test fun `broadcast merge conflicts`() {
    if (!canRunTests()) return

    val note = fakeNote("# Witcher")
    noteQueries.testInsert(note)
    syncer.sync()

    val isConflicted = mergeConflicts.isConflicted(note.id).test()

    RemoteRepositoryRobot {
      pull()
      commitFiles(
        message = "Update witcher.md on remote",
        add = listOf("witcher.md" to "# Witcher\nThe wild hunt")
      )
      forcePush()
    }
    noteQueries.updateContent(
      id = note.id,
      content = "# Witcher\nAssassins of kings",
      updatedAt = clock.nowUtc()
    )

    git.prePushes += {
      assertThat(isConflicted.values.last()).isTrue()
    }
    syncer.sync()
    assertThat(isConflicted.values.last()).isFalse()
  }

  @Test fun `clear merge conflicts if sync fails`() {
    if (!canRunTests()) return

    val noteId = NoteId.generate()
    mergeConflicts.add(noteId)
    val isConflicted = mergeConflicts.isConflicted(noteId).test()

    git.prePulls += { error("boom!") }
    syncer.sync()

    assertThat(isConflicted.values.last()).isFalse()
  }

  @Test fun `backup notes before first sync`() {
    if (!canRunTests()) return

    // Note to self: it is important to use a constant time or else
    // each test will create a new branch in the test repo lol.
    clock.setTime(epochMillis = 1601612911000)
    backupBeforeFirstSync.value = true

    for (note in listOf("I couldn't", "understand half of", "the muffled dialogues", "in Tenet")) {
      clock.advanceTimeBy(1.seconds)
      noteQueries.testInsert(fakeNote(note, clock = clock))
    }
    syncer.sync()

    val remote = RemoteRepositoryRobot {
      pull()
      clock.nowUtc().unixMillisLong
      checkout("notes-backup-1601612911000")
    }
    assertThat(remote.fetchNoteFiles()).containsOnly(
      "untitled_note.md" to "I couldn't",
      "untitled_note_2.md" to "understand half of",
      "untitled_note_3.md" to "the muffled dialogues",
      "untitled_note_4.md" to "in Tenet"
    )
  }

  @Test fun `delete notes after syncing them`() {
    if (!canRunTests()) return

    // Testing two cases:
    // 1. Deletion on first sync
    // 2. Deletion on subsequent syncs.
    val note1 = fakeNote("# Raji 1")
    val note2 = fakeNote("# Raji 2")
    noteQueries.testInsert(note1, note2)
    noteQueries.markAsPendingDeletion(note2.id)
    syncer.sync()

    val remote = RemoteRepositoryRobot()
    assertThat(remote.fetchNoteFiles()).containsOnly("raji_1.md" to "# Raji 1")
    assertThat(noteQueries.allNotes().executeAsList().map { it.id }).containsOnly(note1.id)

    noteQueries.markAsPendingDeletion(note1.id)
    syncer.sync()

    assertThat(remote.fetchNoteFiles()).isEmpty()
    assertThat(noteQueries.allNotes().executeAsList()).isEmpty()
  }

  @Test fun `avoid re-saving locally updated note`() {
    if (!canRunTests()) return

    val note = fakeNote("# Raji")
    noteQueries.testInsert(note)
    assertThat(noteQueries.updateCount.value).isEqualTo(0)

    syncer.sync()
    assertThat(noteQueries.updateCount.value).isEqualTo(0)
  }

  /**
   * Any changes that isn't available on remote are in danger of creating merge conflicts.
   * There was a bug where stale file records were pruned after changes had been pushed to remote.
   * When a note is deleted, it's record isn't deleted alongside. It's instead deleted during pruning
   * and will cause a commit to be made.
   */
  private fun preventCommitsBeforePullsOrAfterPushes() {
    val isDefaultBranch = {
      // This ignores the initial commit for backing-up of notes
      // and announcement commit, which are made on different branches.
      git.repository.currentBranch().name == configQueries.select().executeAsOne().remote.remote.defaultBranch
    }

    var committed = false
    var pushed = false
    git.preCommits += {
      if (isDefaultBranch()) {
        committed = true
        check(!pushed)
      }
    }
    git.prePulls += {
      if (isDefaultBranch()) {
        check(!committed)
      }
    }
    git.postPushes += {
      if (isDefaultBranch()) {
        pushed = true
      }
    }
    git.postHardReset = {
      committed = false
      pushed = false
    }
  }

  private inner class RemoteRepositoryRobot(prepare: RemoteRepositoryRobot.() -> Unit = {}) {
    private val directory = File(deviceInfo.appStorage, "temp").apply { makeDirectory() }
    private val register = FileNameRegister(directory)
    private val gitRepo = RealGit().repository(
      path = directory.path,
      sshKey = remoteAndAuth.sshKey,
      remoteSshUrl = remoteAndAuth.remote.sshUrl,
      userConfig = GitConfig(
        "author" to listOf("name" to "Test remote author", "email" to "press@saket.me"),
        "committer" to listOf("name" to "Test remote committer", "email" to "")
      )
    )

    init {
      gitRepo.commitAll("Initial commit", timestamp = UtcTimestamp(clock), allowEmpty = true)
      gitRepo.checkout(remoteAndAuth.remote.defaultBranch)
      prepare()
    }

    fun pull() {
      gitRepo.pull(rebase = true)
    }

    fun checkout(branch: String) {
      gitRepo.checkout(branch, createIfNeeded = true)
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
