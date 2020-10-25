package me.saket.press.shared.sync

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel.INFO
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.logging.SIMPLE
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType.Application
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.saket.kgit.SshPrivateKey
import me.saket.press.shared.runBlocking

class GitHubRobot(
  private val personalKey: String,
  private val sshUrl: String
) {
  val http = HttpClient {
    install(JsonFeature) {
      serializer = KotlinxSerializer(Json {
        prettyPrint = true
        ignoreUnknownKeys = true
      })
    }
    install(Logging) {
      logger = Logger.SIMPLE
      level = INFO
    }
  }

  fun deleteAndRecreateRepo() {
    // Format: git@github.com:owner/name.git
    val owner = sshUrl.substringAfter(":").substringBefore("/")
    val name = sshUrl.substringAfter("/").substringBefore(".git")

    runBlocking {
      http.delete<String>("https://api.github.com/repos/$owner/$name") {
        header("Authorization", "token $personalKey")
      }

      http.post<String>("https://api.github.com/user/repos") {
        header("Authorization", "token $personalKey")
        contentType(Application.Json)
        body = CreateRepoRequest(name, private = true)
      }
    }
  }
}

@Serializable
private data class CreateRepoRequest(
  val name: String,
  val private: Boolean
)
