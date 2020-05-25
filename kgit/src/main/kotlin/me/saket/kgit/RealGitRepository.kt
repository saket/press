package me.saket.kgit

import org.eclipse.jgit.transport.RemoteRefUpdate
import java.io.File
import org.eclipse.jgit.api.Git as JGit

internal actual class RealGitRepository actual constructor(private val path: String) : GitRepository {

  private val jgit: JGit by lazy {
    // Initializing an exiting git directory no-ops.
    JGit.init().setDirectory(File(path)).call()
  }
  private val currentBranch = "master"

  override fun addAll() {
    jgit.add().addFilepattern("*").call()
  }

  override fun commit(message: String, author: GitAuthor) {
    jgit.commit().apply {
      setMessage(message)
      setAuthor(author.name, author.email)
    }.call()
  }

  override fun push(): PushResult {
    /*
    // add remote repo:
    RemoteAddCommand remoteAddCommand = git.remoteAdd();
    remoteAddCommand.setName("origin");
    remoteAddCommand.setUri(new URIish(httpUrl));
    // you can add more settings here if needed
    remoteAddCommand.call();
    * */

    val pushResult = jgit.push().call()
        .toList()
        .also { check(it.size == 1) { "Did not expect multiple push results: $it" } }
        .single()

    return when (pushResult.getRemoteUpdate("refs/heads/$currentBranch").status) {
      RemoteRefUpdate.Status.OK -> PushResult.Success
      else -> PushResult.Failed(pushResult.toString())
    }
  }
}
