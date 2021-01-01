package me.saket.press.shared.syncer

import kotlinx.serialization.json.Json
import me.saket.kgit.RealGit
import me.saket.press.shared.preferences.sync.stats.SyncStatsForNerdsPresenter
import me.saket.press.shared.syncer.git.GitSyncer
import org.koin.dsl.module

class SharedSyncComponent {
  val module = module {
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
}

internal fun createJson(): Json {
  return Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    useArrayPolymorphism = true
    isLenient = false
  }
}
