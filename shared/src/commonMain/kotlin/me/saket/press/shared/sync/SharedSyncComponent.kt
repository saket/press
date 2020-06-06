package me.saket.press.shared.sync

import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import io.ktor.client.HttpClient
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.serialization.UnstableDefault
import me.saket.kgit.RealGit
import me.saket.press.shared.di.koin
import me.saket.press.shared.editor.AutoCorrectEnabled
import me.saket.press.shared.settings.customTypeSetting
import me.saket.press.shared.sync.git.DeviceId
import me.saket.press.shared.sync.git.DeviceInfo
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.sync.git.GitHub
import me.saket.press.shared.sync.git.GitSyncer
import org.koin.core.qualifier.named
import org.koin.dsl.module

class SharedSyncComponent {

  val module = module {
    single { httpClient() }
    factory { SyncPreferencesPresenter(get()) }
    factory<GitHost> { GitHub(get()) }

    factory { RealGit().repository(get<DeviceInfo>().appStorage.path) }
    factory(named("device_id")) {
      customTypeSetting(
          settings = get(),
          key = "device_id",
          from = { DeviceId(uuidFrom(it)) },
          to = { it.id.toString() },
          defaultValue = DeviceId(uuid4())
      )
    }
    factory<Syncer> { GitSyncer(get(), get(), get(), get(), get(named("device_id"))) }
  }

  @OptIn(UnstableDefault::class)
  private fun httpClient(): HttpClient {
    return HttpClient {
//      install(JsonFeature) {
//        serializer = KotlinxSerializer(Json(JsonConfiguration.Stable.copy(
//            prettyPrint = true,
//            ignoreUnknownKeys = true
//        )))
//      }
      install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
      }
    }
  }

  companion object {
    fun presenter(): SyncPreferencesPresenter = koin()
  }
}
