package me.saket.press.shared.editor

import com.soywiz.klock.seconds
import me.saket.press.shared.di.koin
import me.saket.press.shared.localization.Strings
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object SharedEditorComponent {

  val module = module {
    factory { (args: EditorPresenter.Args) ->
      EditorPresenter(
          args = args,
          noteRepository = get(),
          ioScheduler = get(named("io")),
          computationScheduler = get(named("computation")),
          strings = get<Strings>().editor,
          config = editorConfig()
      )
    }
  }

  fun editorConfig() = EditorConfig(autoSaveEvery = 5.seconds)

  fun presenter(args: EditorPresenter.Args): EditorPresenter = koin { parametersOf(args) }
}
