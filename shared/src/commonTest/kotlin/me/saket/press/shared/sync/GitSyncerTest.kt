package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.containsExactly
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
import me.saket.kgit.GitTreeDiff.Change.Add
import me.saket.kgit.PushResult.Failure
import me.saket.kgit.RealGit
import me.saket.kgit.SshConfig
import me.saket.press.data.shared.Note
import me.saket.press.data.shared.NoteQueries
import me.saket.press.shared.BuildKonfig
import me.saket.press.shared.Platform
import me.saket.press.shared.PlatformHost.Android
import me.saket.press.shared.containsOnly
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.sync.SyncState.PENDING
import me.saket.press.shared.sync.SyncState.SYNCED
import me.saket.press.shared.sync.git.File
import me.saket.press.shared.sync.git.GitSyncer
import me.saket.press.shared.sync.git.UtcTimestamp
import me.saket.press.shared.sync.git.repository
import me.saket.press.shared.testDeviceInfo
import me.saket.press.shared.time.FakeClock
import kotlin.test.AfterTest
import kotlin.test.Test

class GitSyncerTest : BaseDatabaeTest() {

  private val deviceInfo = testDeviceInfo()
  private val noteQueries get() = database.noteQueries
  private val gitDirectory = File(deviceInfo.appStorage, "git")
  private val git = RealGit()
  private val syncer: GitSyncer
  private val clock = FakeClock()

  init {
    println()
    git.ssh = SshConfig(privateKey = BuildKonfig.GITHUB_SSH_PRIV_KEY)

    syncer = GitSyncer(
        git = git.repository(gitDirectory),
        database = database,
        deviceInfo = deviceInfo,
        clock = clock
    )
    syncer.setRemote("git@github.com:saket/PressSyncPlayground.git")
  }

  private fun canRunTests(): Boolean {
    // todo: make tests work for native platforms.
    return BuildKonfig.GITHUB_SSH_PRIV_KEY.isNotBlank() && Platform.host == Android
  }

  @AfterTest
  fun cleanUp() {
    deviceInfo.appStorage.delete(recursively = true)

    RemoteRepositoryRobot {
      commitFiles(message = "Emptiness", add = emptyList())
      forcePush()
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
    val remote = RemoteRepositoryRobot {}

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

  @Test fun `merge local and remote notes without conflicts`() {
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

  @Test fun `resolve conflicts when content has changed but not the file name`() {
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
    assertThat(localNotes[0].content).isEqualTo("# Uncharted\nRemote edit")
    assertThat(localNotes[0].id).isEqualTo(locallyEditedNote.id)

    assertThat(localNotes[1].content).isEqualTo("# Uncharted\nLocal edit")
    assertThat(localNotes[1].id).isNotEqualTo(locallyEditedNote.id)
  }

  @Test fun `resolve conflicts when both the content and file name have changed`() {
    // TODO
  }

  @Test fun `notes with the same title are stored in separate files`() {
    if (!canRunTests()) return
    // TODO
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

  @Test fun `sync notes deleted locally`() {
    if (!canRunTests()) return
    // TODO
  }

  @Test fun `filename-register from remote is used for determining file name`() {
    if (!canRunTests()) return
    // TODO
  }

  @Test fun `ignore notes that are already synced`() {
    if (!canRunTests()) return

    val remote = RemoteRepositoryRobot {}
    noteQueries.testInsert(
        fakeNote(content = "# The Last of Us II", syncState = SYNCED, clock = clock),
        fakeNote(content = "# Horizon Zero Dawn", syncState = PENDING, clock = clock)
    )
    syncer.sync()

    assertThat(remote.fetchNoteFiles()).containsOnly(
        "horizon_zero_dawn.md" to "# Horizon Zero Dawn"
    )
  }

  private inner class RemoteRepositoryRobot(prepare: RemoteRepositoryRobot.() -> Unit) {
    private val directory = File(deviceInfo.appStorage, "temp").apply { makeDirectory() }
    private val gitRepo = git.repository(directory)

    init {
      gitRepo.addRemote("origin", "git@github.com:saket/PressSyncPlayground.git")
      prepare()
    }

    fun pull() {
      gitRepo.pull(rebase = true)
    }

    fun forcePush() {
      assertThat(gitRepo.push(force = true)).isNotInstanceOf(Failure::class)
    }

    fun commitFiles(
      message: String,
      time: DateTime? = null,
      add: List<Pair<String, String>> = emptyList(),
      delete: List<String> = emptyList()
    ) {
      add.forEach { (name, body) ->
        File(directory, name).write(body)
      }
      delete.forEach { path ->
        File(directory, path).apply {
          require(exists) { "$path does not exist" }
          delete()
        }
      }
      gitRepo.commitAll(message, timestamp = UtcTimestamp(time ?: clock.nowUtc()), allowEmpty = true)
    }

    /**
     * @return Pair(file name, file content).
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun fetchNoteFiles(): List<Pair<String, String>> {
      gitRepo.pull(rebase = true)
      val head = gitRepo.headCommit()!!
      val diffs = gitRepo.diffBetween(from = null, to = head)
      return buildList {
        for (diff in diffs) {
          check(diff is Add)  // because we're diffing with an empty file tree.
          if (diff.path.endsWith(".md") && !diff.path.contains("/")) {
            add(diff.path to File(directory, diff.path).read())
          }
        }
      }
    }
  }
}

private fun NoteQueries.testInsert(vararg notes: Note.Impl) {
  notes.forEach { testInsert(it) }
}
