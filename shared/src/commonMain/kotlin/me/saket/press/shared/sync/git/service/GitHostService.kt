package me.saket.press.shared.sync.git.service

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.single.Single
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import me.saket.kgit.SshKeyPair
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.sync.git.GitHostAuthToken
import me.saket.kgit.GitIdentity

/** See [GitHost]. */
interface GitHostService {
  /**
   * Generate a URL that will let the user log into their account
   * and grant Press "write" access to their repositories.
   *
   * @param redirectUrl URL to open once Press has been granted access.
   */
  fun generateAuthUrl(redirectUrl: String): String

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
   * User's details. These are used for making git commits.
   */
  fun fetchUser(token: GitHostAuthToken): Single<GitIdentity>

  /**
   * Add a deploy key to [repository] so that Press
   * can read and write commits for syncing notes.
   */
  fun addDeployKey(token: GitHostAuthToken, repository: GitRepositoryInfo, key: DeployKey): Completable

  /**
   * Create a new repository for syncing notes with Press.
   * If the repository already exists, use it.
   */
  fun createNewRepo(token: GitHostAuthToken, repo: NewGitRepositoryInfo): Single<ApiResult>

  /**
   * @param title the label that's displayed for this key.
   */
  data class DeployKey(val title: String, val key: SshKeyPair)

  fun interface Factory {
    fun create(host: GitHost): GitHostService
  }
}

class GitHostServiceArgs(
  val http: HttpClient,
  val json: Json
)
