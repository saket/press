package me.saket.press.shared.sync.git.service

import kotlinx.serialization.Serializable
import me.saket.press.shared.sync.git.GitHost

/**
 * @param url URL to use for opening the repository in a web browser.
 * @param sshUrl URL to use for cloning the repository.
 */
@Serializable
data class GitRepositoryInfo(
  val host: GitHost,
  val name: String,
  val owner: String,
  val url: String,
  val sshUrl: String,
  val defaultBranch: String
) {

  /**
   * <owner>/<repo-name>.
   * Example: "cashapp/contour".
   */
  val ownerAndName: String get() = "$owner/$name"
}
