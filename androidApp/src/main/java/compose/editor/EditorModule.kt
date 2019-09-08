package compose.editor

import dagger.Module
import dagger.Provides
import me.saket.compose.shared.editor.EditorPresenter
import me.saket.compose.shared.editor.SharedEditorComponent

@Module
object EditorModule {

  @Provides
  @JvmStatic
  fun presenter(): EditorPresenter.Factory {
    return object : EditorPresenter.Factory {
      override fun create() = SharedEditorComponent.presenter()
    }
  }
}