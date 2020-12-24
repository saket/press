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
import me.saket.kgit.GitIdentity
import me.saket.kgit.RealGit
import me.saket.kgit.RealSshKeygen
import me.saket.press.shared.di.koin
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.sync.git.GitHostAuthToken
import me.saket.press.shared.sync.git.GitHostIntegrationPresenter
import me.saket.press.shared.sync.git.GitRepositoryCache
import me.saket.press.shared.sync.git.GitSyncer
import me.saket.press.shared.sync.git.NewGitRepositoryPresenter
import me.saket.press.shared.sync.git.service.GitHostService
import me.saket.press.shared.sync.git.service.GitHostServiceArgs
import me.saket.press.shared.sync.stats.SyncStatsForNerdsPresenter
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class SharedSyncComponent {
  val module = module {
    single { httpClient(get()) }
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
    factory { SyncPreferencesPresenter(get(), get(), get(), get(), get(), get(), get()) }
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

    single { SyncMergeConflicts() }
    factory<Syncer> {
      GitSyncer(
        git = RealGit(),
        database = get(),
        deviceInfo = get(),
        clock = get(),
        strings = get(),
        mergeConflicts = get()
      )
    }
    single<SyncCoordinator> {
      RealSyncCoordinator(get(), get())
    }

    factory {
      SyncStatsForNerdsPresenter(
        syncer = get(),
        strings = get(),
        schedulers = get()
      )
    }
  }

  @OptIn(ExperimentalListener::class)
  private fun gitUserSetting(settings: ObservableSettings, json: Json): Setting<GitIdentity> {
    return Setting.create(
      settings = settings,
      key = "githost_username",
      from = { json.decodeFromString(GitIdentity.serializer(), it) },
      to = { json.encodeToString(GitIdentity.serializer(), it) },
      defaultValue = null
    )
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

  companion object {
    fun preferencesPresenter(): SyncPreferencesPresenter = koin()
    fun statsForNerdsPresenter(): SyncStatsForNerdsPresenter = koin()

    fun integrationPresenter(args: GitHostIntegrationPresenter.Args) =
      koin<GitHostIntegrationPresenter> { parametersOf(args) }

    fun newGitRepositoryPresenter(args: NewGitRepositoryPresenter.Args) =
      koin<NewGitRepositoryPresenter> { parametersOf(args) }
  }
}

internal fun createJson(): Json {
  return Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    useArrayPolymorphism = true
    isLenient = false
  }
}
