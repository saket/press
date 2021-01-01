package me.saket.press.shared.editor

import com.soywiz.klock.seconds
import me.saket.press.shared.di.koin
import me.saket.press.shared.preferences.Setting
import me.saket.wysiwyg.parser.MarkdownParser
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

class SharedEditorComponent {

  val module = module {
    factory { (args: EditorPresenter.Args) ->
      EditorPresenter(
        args = args,
        database = get(),
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
    factory(named("autocorrect")) {
      Setting.create(
        settings = get(),
        key = "autocorrect",
        from = { AutoCorrectEnabled(it.toBoolean()) },
        to = { it.enabled.toString() },
        defaultValue = AutoCorrectEnabled(true)
      )
    }
  }

  companion object {
    fun editorConfig(): EditorConfig =
      EditorConfig(autoSaveEvery = 5.seconds)

    fun presenter(args: EditorPresenter.Args): EditorPresenter =
      koin { parametersOf(args) }

    fun autoCorrectEnabled(): Setting<AutoCorrectEnabled> =
      koin(named("autocorrect"))
  }
}
