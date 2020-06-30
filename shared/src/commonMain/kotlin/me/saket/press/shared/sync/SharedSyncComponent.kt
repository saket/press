package me.saket.press.shared.sync

import com.russhwolf.settings.ExperimentalListener
import com.russhwolf.settings.ObservableSettings
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.logging.SIMPLE
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration.Companion.Stable
import me.saket.kgit.Git
import me.saket.kgit.RealGit
import me.saket.press.shared.di.koin
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.sync.git.GitHostAuthPresenter
import me.saket.press.shared.sync.git.GitHostAuthToken
import me.saket.press.shared.sync.git.GitSyncer
import me.saket.press.shared.sync.git.GitSyncerConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module

class SharedSyncComponent {

  val module = module {
    single { httpClient(get()) }
    single { Json(Stable.copy(prettyPrint = true, ignoreUnknownKeys = true)) }

    factory {
      { host: GitHost ->
        Setting.create(
            settings = get(),
            key = "${host.name}_auth_token",
            from = ::GitHostAuthToken,
            to = GitHostAuthToken::value,
            defaultValue = null
        )
      }
    }
    factory { GitHostAuthPresenter(get(), get(), get(), get(), get(), get(named("io"))) }

    factory<Git> { RealGit() }
    factory<Syncer> { get<GitSyncer>() }
    factory { gitSyncerConfig(get(), get()) }
    factory { GitSyncer(get(), get(), get(), get(), get()) }
  }

  private fun httpClient(json: Json): HttpClient {
    return HttpClient {
      install(JsonFeature) {
        serializer = KotlinxSerializer(json)
      }
      install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
      }
    }
  }

  @OptIn(ExperimentalListener::class)
  private fun gitSyncerConfig(json: Json, settings: ObservableSettings): Setting<GitSyncerConfig> {
    return Setting.create(
        settings = settings,
        key = "gitsyncer_config",
        from = { serialized -> json.parse(GitSyncerConfig.serializer(), serialized) },
        to = { deserialized -> json.stringify(GitSyncerConfig.serializer(), deserialized) },
        defaultValue = null
    )
  }

  companion object {
    fun gitHostAuthPresenter(): GitHostAuthPresenter = koin()
    fun syncer(): Syncer = koin()
  }
}
