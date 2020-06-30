package me.saket.press.shared.sync.git.service

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.single.Single
import me.saket.press.shared.sync.git.GitHostAuthToken
import me.saket.press.shared.sync.git.GitHost

/** See [GitHost]. */
interface GitHostService {
  /**
   * Generate a URL that will let the user log into their account
   * and grant Press "write" access to their repositories.
   */
  fun generateAuthUrl(): String

  /**
   * Called once the user has granted Press access to their repositories
   * and the user was redirected to [callbackUrl], containing an access
   * token in the URL.
   */
  fun completeAuth(callbackUrl: String): Single<GitHostAuthToken>

  /**
   * Repositories owned by the user. Press will display these on the UI
   * and let the user select a repo to use for syncing their notes.
   */
  fun fetchUserRepos(token: GitHostAuthToken): Single<List<GitRepositoryInfo>>

  /**
   * Add a deploy key to [repositoryName] so that Press
   * can read and write commits for syncing notes.
   */
  fun addDeployKey(token: GitHostAuthToken, repositoryName: String, sshPublicKey: String): Completable
}

data class GitRepositoryInfo(val name: String)
