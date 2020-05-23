package me.saket.press.shared.sync.github

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.coroutinesinterop.completableFromCoroutine
import com.badoo.reaktive.coroutinesinterop.singleFromCoroutine
import com.badoo.reaktive.single.Single
import com.benasher44.uuid.uuid4
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.saket.press.shared.sync.Syncer

class GithubSyncer(private val client: HttpClient) : Syncer {

  override fun startUserAuth(): Completable {
    return completableFromCoroutine {
      println("Sending request")
      val response = client.get<String>("https://github.com/login/oauth/authorize") {
        url {
          parameter("client_id", "c8d3f0629f52edce47b6")
          parameter("redirect_uri", "https://github.com/saket/press?finish-login")
          parameter("scope", "repo")
          parameter("state", uuid4().toString())
        }
      }
      println("Received response: $response")
    }
  }
}
