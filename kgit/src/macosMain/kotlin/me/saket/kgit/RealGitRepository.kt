package me.saket.kgit

import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libgit2.ObjectiveGit.*
import platform.Foundation.NSError
import platform.Foundation.NSURL

internal actual class RealGitRepository actual constructor(
  directoryPath: String,
  remote: GitRemote,
  userConfig: GitConfig,
  sshKey: SshPrivateKey
) : GitRepository {

  init {
    /*
    jgit.remoteAdd()
      .setName(remote.name)
      .setUri(URIish(remote.sshUrl))
      .call()

    // Avoid reading any config from [~/.gitconfig] that will lead to non-deterministic
    // behavior on the host machine. For e.g., following of renames may be disabled for
    // computing file diffs.
    JgitSystemReader.getInstance().userConfig.clear()

    val repoConfig = jgit.repository.config
    for (section in userConfig.sections) {
      for ((key, value) in section.values) {
        repoConfig.setString(section.name, null, key, value)
      }
    }
    repoConfig.save()
    * */

    val directoryUrl = NSURL.fileURLWithPath("fake/1234", isDirectory = true)

    memScoped {
      val errorPtr = alloc<ObjCObjectVar<NSError?>>().ptr
      GTRepository.initializeEmptyRepositoryAtFileURL(fileURL = directoryUrl, options = null, error = errorPtr)

      val error = errorPtr.pointed.value
      if (error != null) {
        error(error.errorMessage())
      }
    }
  }

  private fun NSError.errorMessage(): String {
    return "Error: $domain (${code}): $description. User info: $userInfo."
  }

  override fun isStagingAreaDirty(): Boolean = TODO()
  override fun checkout(branch: String, createIfNeeded: Boolean): Unit = TODO()
  override fun checkout(commit: GitCommit): Unit = TODO()
  override fun commitAll(message: String, timestamp: UtcTimestamp, allowEmpty: Boolean) = TODO()
  override fun pull(rebase: Boolean): GitPullResult = TODO()
  override fun push(force: Boolean): PushResult = TODO()
  override fun hardResetTo(sha1: String, resetState: Boolean, deleteUntrackedFiles: Boolean): Unit = TODO()
  override fun headCommit(onBranch: String?): GitCommit = TODO()
  override fun commitsBetween(from: GitCommit?, toInclusive: GitCommit): List<GitCommit> = TODO()
  override fun commonAncestor(first: GitCommit, second: GitCommit): GitCommit? = TODO()
  override fun changesBetween(from: GitCommit?, to: GitCommit): GitTreeDiff = TODO()
  override fun currentBranch(): GitBranch = TODO()
  override fun tryRecovering(e: Throwable): GitErrorRecoveryResult = TODO()
}
