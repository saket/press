package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.isNotInstanceOf
import assertk.assertions.isNull
import com.badoo.reaktive.test.completable.assertComplete
import com.badoo.reaktive.test.completable.test
import com.badoo.reaktive.utils.printStack
import me.saket.kgit.GitRepository
import me.saket.kgit.PushResult
import me.saket.kgit.RealGit
import me.saket.kgit.SshConfig
import me.saket.press.shared.BuildKonfig
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.note.FakeNoteRepository
import me.saket.press.shared.sync.git.AppStorage
import me.saket.press.shared.sync.git.File
import me.saket.press.shared.sync.git.GitSyncer
import me.saket.press.shared.sync.git.repository
import kotlin.test.AfterTest
import kotlin.test.Test

/**
 * See AndroidGitSyncerTest.
 */
abstract class GitSyncerTest(private val appStorage: AppStorage) {

  private val gitDirectory = File(appStorage.path, "git")
  private val git = RealGit()
  private val noteRepository = FakeNoteRepository()
  private val syncer: GitSyncer

  init {
    println()

    git.ssh = SshConfig(privateKey = BuildKonfig.GITHUB_SSH_PRIV_KEY)

    syncer = GitSyncer(
        git = git.repository(gitDirectory),
        noteRepository = noteRepository
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

    with(File(appStorage.path, "temp")) {
      makeDirectory()
      with(git.repository(this)) {
        addRemote("origin", "git@github.com:saket/PressSyncPlayground.git")
        // Add some notes over multiple commits.
        addFilesAndCommit(
            "First commit",
            "note_1.md" to "# The Witcher",
            "note_2.md" to "# Uncharted: The Lost Legacy"
        )
        addFilesAndCommit(
            "Second commit",
            "note_3.md" to "# Overcooked",
            "note_4.md" to "# The Last of Us"
        )
        assertThat(push(force = true)).isNotInstanceOf(PushResult.Failure::class)
      }
    }

    syncer.sync().test().apply {
      error?.printStack()
      assertThat(error).isNull()
      assertComplete()
    }

    println("\nFiles in git directory after syncing:")
    gitDirectory.children().forEach {
      println(it)
    }
  }

  //@Test
  fun `push notes to an empty repo`() {
    if (BuildKonfig.GITHUB_SSH_PRIV_KEY.isBlank()) {
      return
    }

    // Prepare an empty remote repository.
    with(File(appStorage.path, "temp")) {
      makeDirectory()
      with(git.repository(this)) {
        addRemote("origin", "git@github.com:saket/PressSyncPlayground.git")
        assertThat(push(force = true)).isNotInstanceOf(PushResult.Failure::class)
      }
      delete(recursively = true)
    }

    val noteBody = """
      |# Nicolas Cage 
      |is a national treasure
    """.trimMargin()
    noteRepository.savedNotes += fakeNote(content = noteBody)

    syncer.sync().test().apply {
      assertThat(error).isNull()
      assertComplete()
    }
  }

  private fun GitRepository.addFilesAndCommit(message: String, vararg files: Pair<String, String>) {
    files.forEach { (name, body) ->
      File(directoryPath, name).write(body)
    }
    addAll()
    commit(message)
  }
}
