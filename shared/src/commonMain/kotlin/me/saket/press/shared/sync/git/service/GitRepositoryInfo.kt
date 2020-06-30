package me.saket.press.shared.sync.git.service

import kotlinx.serialization.Serializable

/**
 * @param name <user>/<repo-name>. Example: "cashapp/contour".
 * @param url URL to use for opening the repository in a web browser.
 * @param sshUrl URL to use for cloning the repository.
 */
@Serializable
data class GitRepositoryInfo(
  val name: String,
  val url: String,
  val sshUrl: String,
  val defaultBranch: String
)
