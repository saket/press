package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotInstanceOf
import assertk.assertions.isNull
import com.soywiz.klock.DateTime
import com.soywiz.klock.hours
import me.saket.kgit.PushResult.Failure
import me.saket.kgit.RealGit
import me.saket.kgit.SshConfig
import me.saket.kgit.UtcTimestamp
import me.saket.press.shared.BuildKonfig
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.note.archivedAt
import me.saket.press.shared.note.deletedAt
import me.saket.press.shared.sync.git.AppStorage
import me.saket.press.shared.sync.git.File
import me.saket.press.shared.sync.git.GitSyncer
import me.saket.press.shared.sync.git.repository
import me.saket.press.shared.time.FakeClock
import kotlin.test.AfterTest
import kotlin.test.Test

/**
 * See AndroidGitSyncerTest.
 */
abstract class GitSyncerTest(private val appStorage: AppStorage) : BaseDatabaeTest() {

  private val noteQueries get() = database.noteQueries
  private val gitDirectory = File(appStorage.path, "git")
  private val git = RealGit()
  private val syncer: GitSyncer
  private val clock = FakeClock()

  init {
    println()
    git.ssh = SshConfig(privateKey = BuildKonfig.GITHUB_SSH_PRIV_KEY)

    syncer = GitSyncer(
        git = git.repository(gitDirectory),
        database = database
    )
    syncer.setRemote("git@github.com:saket/PressSyncPlayground.git")
  }

  @AfterTest
  fun cleanUp() {
    File(appStorage.path).delete(recursively = true)
  }

//  @Test fun `resolve conflicts when content has changed but not the file name`() {
//    // TODO
//  }
//
//  @Test fun `resolve conflicts when both the content and file name have changed`() {
//    // TODO
//  }

  @Test fun `pull notes on start from a non-empty repo`() {
    if (BuildKonfig.GITHUB_SSH_PRIV_KEY.isBlank()) {
      return
    }

    val firstCommitTime = clock.nowUtc()
    val secondCommitTime = firstCommitTime + 10.hours

    // Given: Remote repository has some notes over multiple commits.
    RemoteRepositoryRobot {
      commitFiles(
          message = "First commit",
          time = firstCommitTime,
          files = listOf(
              "note_1.md" to "# The Witcher",
              "note_2.md" to "# Uncharted: The Lost Legacy"
          )
      )
      commitFiles(
          message = "Second commit",
          time = secondCommitTime,
          files = listOf(
              "note_3.md" to "# Overcooked",
              "note_4.md" to "# The Last of Us"
          )
      )
      forcePush()
    }

    // Given: User hasn't saved any notes on this device yet.
    assertThat(noteQueries.notes().executeAsList()).isEmpty()

    syncer.sync()

    val notesAfterSync = noteQueries.notes().executeAsList()
    assertThat(notesAfterSync.map { it.content }).containsOnly(
        "# The Witcher",
        "# Uncharted: The Lost Legacy",
        "# Overcooked",
        "# The Last of Us"
    )

    notesAfterSync.first { it.content == "# The Witcher" }.apply {
      assertThat(createdAt).isEqualTo(firstCommitTime)
      assertThat(updatedAt).isEqualTo(firstCommitTime)
      assertThat(archivedAt).isNull()
      assertThat(deletedAt).isNull()
    }

    notesAfterSync.first { it.content == "# The Last of Us" }.apply {
      assertThat(createdAt).isEqualTo(secondCommitTime)
    }
  }

  @Test
  fun `push notes to an empty repo`() {
    if (BuildKonfig.GITHUB_SSH_PRIV_KEY.isBlank()) {
      return
    }

    // Given: Remote repository is empty.
    RemoteRepositoryRobot {
      commitFiles(message = "Emptiness", files = emptyList())
      forcePush()
    }

    // Given: This device has non-zero notes.
    val noteBody = """
      |# Nicolas Cage 
      |is a national treasure
    """.trimMargin()
    noteQueries.testInsert(fakeNote(content = noteBody))

    syncer.sync()
  }

  private inner class RemoteRepositoryRobot(prepare: RemoteRepositoryRobot.() -> Unit) {
    private val directory = File(appStorage.path, "temp").apply { makeDirectory() }
    private val gitRepo = git.repository(directory)

    init {
      gitRepo.addRemote("origin", "git@github.com:saket/PressSyncPlayground.git")
      prepare()
    }

    fun forcePush() {
      assertThat(gitRepo.push(force = true)).isNotInstanceOf(Failure::class)
    }

    fun commitFiles(message: String, time: DateTime? = null, files: List<Pair<String, String>>) {
      files.forEach { (name, body) ->
        File(directory.path, name).write(body)
      }
      gitRepo.addAll()
      gitRepo.commit(message, timestamp = time?.unixMillisLong?.let(::UtcTimestamp), allowEmpty = true)
    }
  }
}
