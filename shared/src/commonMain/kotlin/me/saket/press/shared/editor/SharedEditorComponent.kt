package me.saket.press.shared.editor

import com.soywiz.klock.seconds
import me.saket.press.shared.di.koin
import me.saket.press.shared.editor.folder.MoveToFolderPresenter
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
    factory { (args: MoveToFolderPresenter.Args) ->
      MoveToFolderPresenter(
        args = args,
        database = get()
      )
    }
  }

  companion object {
    fun editorConfig(): EditorConfig =
      EditorConfig(autoSaveEvery = 5.seconds)

    fun editorPresenter(args: EditorPresenter.Args): EditorPresenter =
      koin { parametersOf(args) }

    fun moveToFolderPresenter(args: MoveToFolderPresenter.Args): MoveToFolderPresenter =
      koin { parametersOf(args) }
  }
}
