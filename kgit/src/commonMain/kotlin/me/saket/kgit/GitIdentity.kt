package me.saket.kgit

import kotlinx.serialization.Serializable

@Serializable
data class GitIdentity(
  val name: String,
  val email: String?
)
