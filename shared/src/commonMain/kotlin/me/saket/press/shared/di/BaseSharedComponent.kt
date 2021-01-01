package me.saket.press.shared.di

import me.saket.press.shared.SharedAppComponent
import me.saket.press.shared.db.SharedDatabaseComponent
import me.saket.press.shared.editor.SharedEditorComponent
import me.saket.press.shared.home.SharedHomeComponent
import me.saket.press.shared.preferences.SharedPreferencesComponent
import me.saket.press.shared.syncer.SharedSyncComponent
import me.saket.press.shared.syncer.SyncCoordinator
import org.koin.core.context.startKoin

expect object SharedComponent : BaseSharedComponent

abstract class BaseSharedComponent {
  fun setupGraph(platform: PlatformDependencies) {
    startKoin {
      modules(
        listOf(
          SharedAppComponent().module,
          SharedHomeComponent().module,
          SharedEditorComponent().module,
          SharedDatabaseComponent().module,
          SharedSyncComponent().module,
          SharedPreferencesComponent().module,
          platform.asKoinModule()
        )
      )
    }

    koin<SyncCoordinator>().start()
  }
}
