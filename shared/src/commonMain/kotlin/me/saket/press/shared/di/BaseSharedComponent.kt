package me.saket.press.shared.di

import me.saket.press.shared.SharedAppComponent
import me.saket.press.shared.db.SharedDatabaseComponent
import me.saket.press.shared.editor.SharedEditorComponent
import me.saket.press.shared.home.SharedHomeComponent
import me.saket.press.shared.note.PrePopulatedNotes
import me.saket.press.shared.note.SharedNoteComponent
import me.saket.press.shared.sync.SharedSyncComponent
import me.saket.press.shared.sync.SyncCoordinator
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
              SharedNoteComponent().module,
              SharedDatabaseComponent().module,
              SharedSyncComponent().module,
              platform.asKoinModule()
          )
      )
    }

    koin<PrePopulatedNotes>().doWork()
    koin<SyncCoordinator>().doWork()
  }
}
