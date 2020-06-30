package me.saket.kgit

import kotlinx.serialization.Serializable

data class SshKeyPair(
  val publicKey: String,
  val privateKey: SshPrivateKey
)

@Serializable
data class SshPrivateKey(val key: String)
