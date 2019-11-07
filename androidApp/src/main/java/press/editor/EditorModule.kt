package press.editor

import dagger.Module
import dagger.Provides
import me.saket.press.shared.editor.EditorOpenMode
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.SharedEditorComponent
import me.saket.press.shared.localization.SharedLocalizationComponent
import me.saket.press.shared.localization.Strings

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