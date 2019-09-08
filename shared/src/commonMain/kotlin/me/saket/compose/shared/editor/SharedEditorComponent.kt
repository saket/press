package me.saket.compose.shared.editor

import me.saket.compose.shared.di.koin
import org.koin.dsl.module

object SharedEditorComponent {

  val module = module {
    factory { EditorPresenter() }
  }

  fun presenter(): EditorPresenter = koin()
}