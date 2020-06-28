package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.coroutinesinterop.singleFromCoroutine
import com.badoo.reaktive.single.Single
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import me.saket.press.shared.BuildKonfig

class GitHub(private val client: HttpClient) : GitHost {

  override fun generateAuthUrl(): String {
    return URLBuilder("https://github.com/login/oauth/authorize").apply {
      parameters.apply {
        append("client_id", BuildKonfig.GITHUB_CLIENT_ID)
        append("scope", "repo")
      }
    }.buildString()
  }

  override fun completeAuth(callbackUrl: String): Single<Authorized> {
    return singleFromCoroutine {
      val response = client.post<GetAccessTokenResponse>("https://github.com/login/oauth/access_token") {
        contentType(ContentType.Application.Json)
        body = GetAccessTokenRequest(
            client_id = BuildKonfig.GITHUB_CLIENT_ID,
            client_secret = BuildKonfig.GITHUB_CLIENT_SECRET,
            code = Url(callbackUrl).parameters["code"]!!
        )
      }
      Authorized(response.access_token)
    }
  }

  class Authorized(private val accessToken: String) : GitHost.Authorized {
    override fun addDeployKey(repositoryName: String, sshPublicKey: String): Completable {
      return completableFromFunction {
        println("TODO: Add deploy key. Access token: $accessToken")
      }
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
