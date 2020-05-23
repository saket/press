package me.saket.press.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import me.saket.press.shared.di.koin
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.sync.github.GithubSyncer
import me.saket.press.shared.sync.Syncer
import org.koin.dsl.module

class SharedNetworkComponent {
  val module = module {
    single<HttpClient> { httpClient() }
    single<Syncer> { GithubSyncer(get()) }
  }

  private fun httpClient(): HttpClient {
    return HttpClient {
//      install(JsonFeature) {
//        serializer = KotlinxSerializer()
//      }
      install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
      }
    }
  }

  companion object {
    fun syncer(): Syncer = koin()
  }
}
