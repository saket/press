package press.editor

import dagger.Module
import dagger.Provides
import me.saket.press.shared.editor.AutoCorrectEnabled
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.SharedEditorComponent
import me.saket.press.shared.preferences.Setting

@Module
object EditorModule {

  @Provides
  fun presenter() = EditorPresenter.Factory { SharedEditorComponent.presenter(it) }

  @Provides
  fun autoCorrectEnabled(): Setting<AutoCorrectEnabled> = SharedEditorComponent.autoCorrectEnabled()
}
