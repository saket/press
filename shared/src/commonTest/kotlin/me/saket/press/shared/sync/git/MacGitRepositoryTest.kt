package me.saket.press.shared.sync.git

import me.saket.kgit.GitConfig
import me.saket.kgit.GitIdentity
import me.saket.kgit.GitRepository
import me.saket.kgit.RealGit
import me.saket.kgit.SshPrivateKey
import me.saket.press.shared.BuildKonfig
import me.saket.press.shared.Platform
import me.saket.press.shared.PlatformHost.macOS
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.fakedata.fakeRepository
import me.saket.press.shared.localization.ENGLISH_STRINGS
import me.saket.press.shared.sync.SyncMergeConflicts
import me.saket.press.shared.testDeviceInfo
import me.saket.press.shared.time.FakeClock
import kotlin.test.Test

class MacGitRepositoryTest : BaseDatabaeTest() {
  val repository: GitRepository

  init {
    val remote = GitRemoteAndAuth(
      remote = fakeRepository().copy(
        sshUrl = BuildKonfig.GIT_TEST_REPO_SSH_URL,
        defaultBranch = BuildKonfig.GIT_TEST_REPO_BRANCH
      ),
      sshKey = SshPrivateKey(BuildKonfig.GIT_TEST_SSH_PRIV_KEY),
      user = GitIdentity(name = "Test syncer author", email = "test@test.com")
    )
    val deviceInfo = testDeviceInfo()

    println("git dir: ${deviceInfo.appStorage.path}")

    repository = RealGit().repository(
      path = deviceInfo.appStorage.path,
      sshKey = remote.sshKey,
      remoteSshUrl = remote.remote.sshUrl,
      userConfig = GitConfig(
        "author" to listOf("name" to remote.user.name, "email" to (remote.user.email ?: "")),
        "committer" to listOf("name" to "press", "email" to "press@saket.me"),
        "diff" to listOf("renames" to "true")
      )
    )
  }

  @Test fun canary() {

  }

  private fun canRunTests(): Boolean {
    return BuildKonfig.GIT_TEST_SSH_PRIV_KEY.isNotBlank() && Platform.host == macOS
  }
}
