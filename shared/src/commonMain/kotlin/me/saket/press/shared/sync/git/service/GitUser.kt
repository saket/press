package me.saket.press.shared.sync.git.service

import kotlinx.serialization.Serializable

@Serializable
data class GitUser(
  val username: String,
  val email: String?
)
