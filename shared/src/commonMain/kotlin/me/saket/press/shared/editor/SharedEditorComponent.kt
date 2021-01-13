package me.saket.press.shared.editor

import com.soywiz.klock.seconds
import me.saket.press.shared.di.koin
import me.saket.wysiwyg.parser.MarkdownParser
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class SharedEditorComponent {
  val module = module {
    factory { (args: EditorPresenter.Args) ->
      EditorPresenter(
        args = args,
        database = get(),
        syncer = get(),
        clock = get(),
        schedulers = get(),
        strings = get(),
        config = editorConfig(),
        syncConflicts = get(),
        markdownParser = MarkdownParser(),
        clipboard = get(),
        deviceInfo = get()
      )
    }
  }

  companion object {
    fun editorConfig(): EditorConfig =
      EditorConfig(autoSaveEvery = 5.seconds)

    fun presenter(args: EditorPresenter.Args): EditorPresenter =
      koin { parametersOf(args) }
  }
}
