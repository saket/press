package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.coroutinesinterop.completableFromCoroutine
import com.benasher44.uuid.uuid4
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import me.saket.press.shared.BuildKonfig

class GitHub(private val client: HttpClient) : GitHost {

  override fun authorizationRequestUrl(): String {
    return URLBuilder("https://github.com/login/oauth/authorize").apply {
        parameters.apply {
          append("client_id", BuildKonfig.GITHUB_CLIENT_ID)
          append("scope", "repo")
        }
      }.buildString()
  }

  override fun completeAuthorization(callbackUrl: String): Completable {
    return completableFromCoroutine {
      val responseUrl = Url(callbackUrl)

      val response = client.post<String>("https://github.com/login/oauth/access_token") {
        url {
          parameter("client_id", BuildKonfig.GITHUB_CLIENT_ID)
          parameter("client_secret", BuildKonfig.GITHUB_CLIENT_SECRET)
          parameter("code", responseUrl.parameters["code"])
        }
      }
      println("Auth response: $response")
    }
  }
}
