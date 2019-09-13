package me.saket.compose.shared.editor

import me.saket.compose.shared.di.koin
import me.saket.compose.shared.localization.Strings
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object SharedEditorComponent {

  val module = module {
    factory { (openMode: EditorOpenMode) -> EditorPresenter(
        openMode = openMode,
        noteRepository = get(),
        ioScheduler = get(named("io")),
        strings = get<Strings>().editor
    ) }
  }

  fun presenter(openMode: EditorOpenMode): EditorPresenter = koin { parametersOf(openMode) }
}