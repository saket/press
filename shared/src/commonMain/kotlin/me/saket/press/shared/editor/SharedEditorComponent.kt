package me.saket.press.shared.editor

import com.soywiz.klock.seconds
import me.saket.press.shared.di.koin
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.settings.customTypeSetting
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

class SharedEditorComponent {

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
    factory(named("autocorrect")) {
      customTypeSetting(
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
