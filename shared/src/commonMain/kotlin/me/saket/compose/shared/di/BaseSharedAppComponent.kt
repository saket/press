package me.saket.compose.shared.di

import me.saket.compose.shared.db.SharedDatabaseComponent
import me.saket.compose.shared.editor.SharedEditorComponent
import me.saket.compose.shared.home.SharedHomeComponent
import me.saket.compose.shared.localization.SharedLocalizationComponent
import me.saket.compose.shared.note.SharedNoteComponent
import me.saket.compose.shared.time.SharedTimeModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module

expect object SharedAppComponent : BaseSharedAppComponent

abstract class BaseSharedAppComponent {

  fun setupGraph(platformDependencies: Module) {
    startKoin {
      modules(
          listOf(
              SharedHomeComponent.module,
              SharedEditorComponent.module,
              SharedNoteComponent.module,
              SharedDatabaseComponent.module,
              SharedTimeModule.module,
              SharedLocalizationComponent.module
          ) + platformDependencies
      )
    }
  }
}
