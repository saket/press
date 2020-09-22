package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.json.Json
import me.saket.kgit.GitIdentity
import me.saket.kgit.SshPrivateKey
import me.saket.press.shared.fakedata.fakeRepository
import me.saket.press.shared.sync.git.GitSyncerConfig
import kotlin.test.Test

class GitSyncerConfigTest {

  /** If this test fails then it means a migration is needed for old users. */
  @Test fun `serialize and deserialize`() {
    val json = Json(Json.Default) {
      ignoreUnknownKeys = true
      isLenient = false
    }

    val serialized = """
      |{
      |  "remote": {
      |    "name": "nationaltreasure",
      |    "owner": "cage",
      |    "url": "https://github.com/cage/nationaltreasure",
      |    "sshUrl": "git@github.com:cage/nationaltreasure.git",
      |    "defaultBranch": "trunk"
      |  },
      |  "sshKey": {
      |    "key": "nicolascage"
      |  },
      |  "user": {
      |    "name": "niccage",
      |    "email": "nicolas@ca.ge"
      |  }
      |}
      """.trimMargin()
    val deserialized = json.decodeFromString(GitSyncerConfig.serializer(), serialized)
    assertThat(deserialized).isEqualTo(
        GitSyncerConfig(
            sshKey = SshPrivateKey("nicolascage"),
            remote = fakeRepository(),
            user = GitIdentity(
                name = "niccage",
                email = "nicolas@ca.ge"
            )
        )
    )
  }
}
