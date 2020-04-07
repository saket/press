package press.editor

import dagger.Module
import dagger.Provides
import me.saket.press.shared.editor.AutoCorrectEnabled
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.SharedEditorComponent
import me.saket.press.shared.localization.SharedLocalizationComponent
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.settings.Setting

@Module
object EditorModule {

  @Provides
  fun presenter(): EditorPresenter.Factory {
    // SAM conversion of Kotlin interfaces would have been nice.
    return object : EditorPresenter.Factory {
      override fun create(args: EditorPresenter.Args) = SharedEditorComponent.presenter(args)
    }
  }

  @Provides
  fun strings(): Strings.Editor = SharedLocalizationComponent.strings().editor

  @Provides
  fun autoCorrectEnabled(): Setting<AutoCorrectEnabled> = SharedEditorComponent.autoCorrectEnabled()
}
