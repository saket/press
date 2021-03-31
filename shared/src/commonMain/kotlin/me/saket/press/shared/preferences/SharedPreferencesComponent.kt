package me.saket.press.shared.preferences

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel.INFO
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.logging.SIMPLE
import kotlinx.serialization.json.Json
import me.saket.kgit.GitIdentity
import me.saket.kgit.RealSshKeygen
import me.saket.press.shared.di.koin
import me.saket.press.shared.preferences.sync.SyncPreferencesPresenter
import me.saket.press.shared.preferences.sync.stats.SyncStatsForNerdsPresenter
import me.saket.press.shared.syncer.createJson
import me.saket.press.shared.syncer.git.GitHost
import me.saket.press.shared.syncer.git.GitHostAuthToken
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationPresenter
import me.saket.press.shared.preferences.sync.setup.GitRepositoryCache
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryPresenter
import me.saket.press.shared.syncer.git.service.GitHostService
import me.saket.press.shared.syncer.git.service.GitHostServiceArgs
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class SharedPreferencesComponent {
  val module = module {
    single { httpClient(get(), get()) }
    single { createJson() }

    factory {
      GitHostService.Factory { it.service(GitHostServiceArgs(get(), get())) }
    }

    factory { // Note to self: this probably blocks all future <(GitHost) -> *> definitions.
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
    factory { (args: SyncPreferencesPresenter.Args) ->
      SyncPreferencesPresenter(
        args = args,
        syncer = get(),
        gitHostService = get(),
        schedulers = get(),
        authToken = get(),
        clock = get(),
        strings = get(),
        cachedRepos = get()
      )
    }
    factory { (args: GitHostIntegrationPresenter.Args) ->
      GitHostIntegrationPresenter(
        args = args,
        authToken = get(),
        gitHostService = get(),
        userSetting = gitUserSetting(settings = get(), json = get()),
        deviceInfo = get(),
        database = get(),
        cachedRepos = get(),
        syncCoordinator = get(),
        sshKeygen = RealSshKeygen(),
        screenResults = get()
      )
    }
    factory { (args: NewGitRepositoryPresenter.Args) ->
      NewGitRepositoryPresenter(
        args = args,
        authToken = get(),
        strings = get(),
        gitHostService = get(),
      )
    }
  }

  @OptIn(ExperimentalSettingsApi::class)
  private fun gitUserSetting(settings: ObservableSettings, json: Json): Setting<GitIdentity> {
    return Setting.create(
      settings = settings,
      key = "githost_username",
      from = { json.decodeFromString(GitIdentity.serializer(), it) },
      to = { json.encodeToString(GitIdentity.serializer(), it) },
      defaultValue = null
    )
  }

  private fun httpClient(json: Json, platformEngine: HttpClientEngine): HttpClient {
    return HttpClient(platformEngine) {
      followRedirects = true

      install(JsonFeature) {
        serializer = KotlinxSerializer(json)
      }
      install(Logging) {
        logger = Logger.SIMPLE
        level = INFO
      }
    }
  }

  companion object {
    fun preferencesPresenter(args: SyncPreferencesPresenter.Args) =
      koin<SyncPreferencesPresenter> { parametersOf(args) }

    fun statsForNerdsPresenter(): SyncStatsForNerdsPresenter = koin()

    fun integrationPresenter(args: GitHostIntegrationPresenter.Args) =
      koin<GitHostIntegrationPresenter> { parametersOf(args) }

    fun newGitRepositoryPresenter(args: NewGitRepositoryPresenter.Args) =
      koin<NewGitRepositoryPresenter> { parametersOf(args) }
  }
}
