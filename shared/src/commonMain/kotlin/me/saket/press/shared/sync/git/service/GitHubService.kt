package me.saket.press.shared.sync.git.service

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.coroutinesinterop.completableFromCoroutine
import com.badoo.reaktive.coroutinesinterop.singleFromCoroutine
import com.badoo.reaktive.single.Single
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.ContentType.Application
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseList
import me.saket.press.shared.BuildKonfig
import me.saket.press.shared.sync.git.GitHostAuthToken

class GitHubService(
  private val client: HttpClient,
  private val json: Json
) : GitHostService {

  override fun generateAuthUrl(): String {
    return URLBuilder("https://github.com/login/oauth/authorize").apply {
      parameters.apply {
        append("client_id", BuildKonfig.GITHUB_CLIENT_ID)
        append("scope", "repo")
      }
    }.buildString()
  }

  override fun completeAuth(callbackUrl: String): Single<GitHostAuthToken> {
    return singleFromCoroutine {
      val response = client.post<GetAccessTokenResponse>("https://github.com/login/oauth/access_token") {
        contentType(Application.Json)
        body = GetAccessTokenRequest(
            client_id = BuildKonfig.GITHUB_CLIENT_ID,
            client_secret = BuildKonfig.GITHUB_CLIENT_SECRET,
            code = Url(callbackUrl).parameters["code"]!!
        )
      }
      GitHostAuthToken(response.access_token)
    }
  }

  @OptIn(ImplicitReflectionSerializer::class, ExperimentalStdlibApi::class)
  override fun fetchUserRepos(token: GitHostAuthToken): Single<List<GitRepositoryInfo>> {
    return singleFromCoroutine {
      var pageNum = 1
      var hasNextPage = true

      return@singleFromCoroutine buildList {
        while (hasNextPage) {
          val response = client.get<HttpResponse>("https://api.github.com/user/repos") {
            accept(Application.Json)
            header("Authorization", "token ${token.value}")
            parameter("per_page", "50")
            parameter("page", "${pageNum++}")
          }

          val responseBody = json.parseList<GitHubRepo>(response.readText())
          addAll(responseBody.map { GitRepositoryInfo(it.full_name) })

          hasNextPage = "rel=\"next\"" in response.headers["Link"].orEmpty()
        }
      }
    }
  }

  override fun addDeployKey(token: GitHostAuthToken, repositoryName: String, sshPublicKey: String): Completable {
    return completableFromCoroutine {
      check('/' in repositoryName)  // <user>/<repo-name>
      val response = client.post<String>("https://api.github.com/repos/$repositoryName/keys") {
        header("Authorization", "token ${token.value}")
        contentType(Application.Json)
        body = CreateDeployKeyRequest(
            title = "Press",
            key = sshPublicKey,
            read_only = false
        )
      }
      println("response: $response")
    }
  }
}

@Serializable
private data class GetAccessTokenRequest(
  val client_id: String,
  val client_secret: String,
  val code: String
)

@Serializable
private data class GetAccessTokenResponse(
  val access_token: String
)

@Serializable
private data class GitHubRepo(
  val full_name: String
)

@Serializable
private data class CreateDeployKeyRequest(
  val title: String,
  val key: String,
  val read_only: Boolean
)
