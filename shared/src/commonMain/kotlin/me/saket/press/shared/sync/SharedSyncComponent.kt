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
import me.saket.kgit.RealGit
import me.saket.press.shared.db.DateTimeAdapter
import me.saket.press.shared.di.koin
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.sync.git.GitHostAuthToken
import me.saket.press.shared.sync.git.GitHostIntegrationPresenter
import me.saket.press.shared.sync.git.GitRepositoryCache
import me.saket.press.shared.sync.git.GitSyncer
import me.saket.press.shared.sync.git.GitSyncerConfig
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

class SharedSyncComponent {

  val module = module {
    single { httpClient(get()) }
    single {
      Json(from = Json.Default) {
        prettyPrint = true
        ignoreUnknownKeys = true
        useArrayPolymorphism = true
        isLenient = false
      }
    }

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
    single<GitRepositoryCache> { GitRepositoryCache.InMemory() }
    factory { SyncPreferencesPresenter(get(), get(), get(), get(), get(), get(), get()) }
    factory { (args: GitHostIntegrationPresenter.Args) ->
      GitHostIntegrationPresenter(
          args = args,
          httpClient = get(),
          authToken = get(),
          cachedRepos = get(),
          syncCoordinator = get(),
          syncerConfig = get(named("gitsyncer_config"))
      )
    }

    factory<Syncer> { get<GitSyncer>() }
    factory(named("gitsyncer_config")) { gitSyncerConfig(get(), get()) }
    single { SyncMergeConflicts() }
    factory {
      GitSyncer(
          git = RealGit(),
          config = get(named("gitsyncer_config")),
          database = get(),
          deviceInfo = get(),
          clock = get(),
          lastSyncedAt = lastSyncedAt(get()),
          lastPushedSha1 = lastPushedSha1(get()),
          strings = get(),
          mergeConflicts = get()
      )
    }

    single<SyncCoordinator> { RealSyncCoordinator(get(), get()) }
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
        from = { serialized -> json.decodeFromString(GitSyncerConfig.serializer(), serialized) },
        to = { deserialized -> json.encodeToString(GitSyncerConfig.serializer(), deserialized) },
        defaultValue = null
    )
  }

  @OptIn(ExperimentalListener::class)
  private fun lastSyncedAt(settings: ObservableSettings): Setting<LastSyncedAt> {
    return Setting.create(
        settings = settings,
        key = "last_synced_at",
        from = { LastSyncedAt(DateTimeAdapter.decode(it)) },
        to = { DateTimeAdapter.encode(it.value) },
        defaultValue = null
    )
  }

  @OptIn(ExperimentalListener::class)
  private fun lastPushedSha1(settings: ObservableSettings): Setting<LastPushedSha1> {
    return Setting.create(
        settings = settings,
        key = "last_pushed_sha1",
        from = { LastPushedSha1(it) },
        to = { it.sha1 },
        defaultValue = null
    )
  }

  companion object {
    fun preferencesPresenter(): SyncPreferencesPresenter = koin()

    fun integrationPresenter(args: GitHostIntegrationPresenter.Args) =
      koin<GitHostIntegrationPresenter> { parametersOf(args) }
  }
}
