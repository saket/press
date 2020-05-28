package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.isNull
import com.badoo.reaktive.test.base.assertNotError
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

abstract class GitSyncerTest(appStorage: AppStorage) {

  private val git = RealGit()
  private val noteRepository = FakeNoteRepository()
  private val syncer = GitSyncer(git, appStorage, noteRepository)

  init {
    git.ssh = SshConfig(privateKey = BuildKonfig.GITHUB_SSH_PRIV_KEY)
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
