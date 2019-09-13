package compose.editor

import dagger.Module
import dagger.Provides
import me.saket.compose.shared.editor.EditorOpenMode
import me.saket.compose.shared.editor.EditorPresenter
import me.saket.compose.shared.editor.SharedEditorComponent
import me.saket.compose.shared.localization.SharedLocalizationComponent
import me.saket.compose.shared.localization.Strings

@Module
object EditorModule {

  @Provides
  @JvmStatic
  fun presenter(): EditorPresenter.Factory {
    return object : EditorPresenter.Factory {
      override fun create(openMode: EditorOpenMode) = SharedEditorComponent.presenter(openMode)
    }
  }

  @Provides
  @JvmStatic
  fun strings(): Strings.Editor = SharedLocalizationComponent.strings().editor
}