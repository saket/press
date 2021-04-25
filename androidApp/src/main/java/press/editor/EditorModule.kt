package press.editor

import dagger.Module
import dagger.Provides
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.SharedEditorComponent
import me.saket.press.shared.editor.folder.MoveToFolderPresenter

@Module
object EditorModule {
  @Provides
  fun editorPresenter() = EditorPresenter.Factory { SharedEditorComponent.editorPresenter(it) }

  @Provides
  fun moveToFolderPresenter() = MoveToFolderPresenter.Factory { SharedEditorComponent.moveToFolderPresenter(it) }
}
