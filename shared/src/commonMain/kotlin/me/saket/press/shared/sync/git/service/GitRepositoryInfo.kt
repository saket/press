package me.saket.press.shared.sync.git.service

import kotlinx.serialization.Serializable

@Serializable
data class GitRepositoryInfo(
  val name: String,
  val sshUrl: String,
  val defaultBranch: String
)
