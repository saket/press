package me.saket.press.shared.sync.git

import com.squareup.sqldelight.ColumnAdapter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.saket.kgit.SshPrivateKey
import me.saket.press.shared.sync.git.service.GitRepositoryInfo
import me.saket.kgit.GitIdentity

// TODO: Maybe RemoteConfig?
@Serializable
data class GitSyncerConfig(
  val remote: GitRepositoryInfo,
  val sshKey: SshPrivateKey,
  val user: GitIdentity
) {

  class SqlAdapter(private val json: Json) : ColumnAdapter<GitSyncerConfig, String> {
    override fun decode(databaseValue: String) = json.decodeFromString(serializer(), databaseValue)
    override fun encode(value: GitSyncerConfig) = json.encodeToString(serializer(), value)
  }
}
