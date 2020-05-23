package me.saket.press.shared.di

import me.saket.press.shared.db.SharedDatabaseComponent
import me.saket.press.shared.editor.SharedEditorComponent
import me.saket.press.shared.home.SharedHomeComponent
import me.saket.press.shared.keyboard.SharedKeyboardComponent
import me.saket.press.shared.localization.SharedLocalizationComponent
import me.saket.press.shared.network.SharedNetworkComponent
import me.saket.press.shared.note.PrePopulatedNotes
import me.saket.press.shared.note.SharedNoteComponent
import me.saket.press.shared.time.SharedTimeComponent
import org.koin.core.context.startKoin

expect object SharedAppComponent : BaseSharedAppComponent

abstract class BaseSharedAppComponent {
  fun setupGraph(platform: PlatformDependencies) {
    startKoin {
      modules(
          listOf(
              SharedHomeComponent().module,
              SharedEditorComponent().module,
              SharedNoteComponent().module,
              SharedDatabaseComponent().module,
              SharedTimeComponent().module,
              SharedLocalizationComponent().module,
              SharedKeyboardComponent().module,
              SharedNetworkComponent().module,
              platform.asKoinModule()
          )
      )
    }

    koin<PrePopulatedNotes>().doWork()
  }
}
