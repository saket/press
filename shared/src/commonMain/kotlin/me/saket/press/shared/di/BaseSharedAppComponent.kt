package me.saket.press.shared.di

import me.saket.press.shared.db.SharedDatabaseComponent
import me.saket.press.shared.editor.SharedEditorComponent
import me.saket.press.shared.home.SharedHomeComponent
import me.saket.press.shared.localization.SharedLocalizationComponent
import me.saket.press.shared.note.PrePopulatedNotes
import me.saket.press.shared.note.SharedNoteComponent
import me.saket.press.shared.time.SharedTimeModule
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

    koin<PrePopulatedNotes>().doWork()
  }
}
