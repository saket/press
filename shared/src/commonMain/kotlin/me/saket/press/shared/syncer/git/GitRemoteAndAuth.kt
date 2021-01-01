package me.saket.press.shared.syncer.git

import com.squareup.sqldelight.ColumnAdapter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.saket.kgit.SshPrivateKey
import me.saket.press.shared.syncer.git.service.GitRepositoryInfo
import me.saket.kgit.GitIdentity

@Serializable
data class GitRemoteAndAuth(
  val remote: GitRepositoryInfo,
  val sshKey: SshPrivateKey,
  val user: GitIdentity
) {

  class SqlAdapter(private val json: Json) : ColumnAdapter<GitRemoteAndAuth, String> {
    override fun decode(databaseValue: String) = json.decodeFromString(serializer(), databaseValue)
    override fun encode(value: GitRemoteAndAuth) = json.encodeToString(serializer(), value)
  }
}
