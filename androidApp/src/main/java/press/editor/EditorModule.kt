package press.editor

import dagger.Module
import dagger.Provides
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.SharedEditorComponent

@Module
object EditorModule {
  @Provides
  fun presenter() = EditorPresenter.Factory { SharedEditorComponent.presenter(it) }
}
