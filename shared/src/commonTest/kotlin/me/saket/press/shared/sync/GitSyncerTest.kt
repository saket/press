package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.isNull
import com.badoo.reaktive.test.completable.assertComplete
import com.badoo.reaktive.test.completable.test
import me.saket.kgit.RealGit
import me.saket.kgit.SshConfig
import me.saket.press.shared.BuildKonfig
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.note.FakeNoteRepository
import me.saket.press.shared.sync.git.AppStorage
import me.saket.press.shared.sync.git.GitSyncer
import kotlin.test.Test

/**
 * See AndroidGitSyncerTest.
 */
abstract class GitSyncerTest(appStorage: AppStorage) {

  private val git = RealGit()
  private val noteRepository = FakeNoteRepository()
  private val syncer = GitSyncer(git, appStorage, noteRepository)

  init {
    git.ssh = SshConfig(privateKey = BuildKonfig.GITHUB_SSH_PRIV_KEY)
  }

  @Test fun `resolve conflicts when content has changed but not the file name`() {
    // TODO
  }

  @Test fun `resolve conflicts when both the content and file name have changed`() {
    // TODO
  }

  @Test fun `pull notes on start`() {
    // TODO
  }

  @Test fun `push notes`() {
    if (BuildKonfig.GITHUB_SSH_PRIV_KEY.isBlank()) {
      return
    }

    val noteBody = """
      |# Nicolas Cage 
      |is a national treasure
    """.trimMargin()
    noteRepository.savedNotes += fakeNote(content = noteBody)

    syncer.setRemote("git@github.com:saket/PressSyncPlayground.git")
    syncer.sync().test().apply {
      assertThat(error).isNull()
      assertComplete()
    }
  }
}
