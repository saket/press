package compose.editor

import com.benasher44.uuid.Uuid
import dagger.Module
import dagger.Provides
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
      override fun create(noteUuid: Uuid) = SharedEditorComponent.presenter(noteUuid)
    }
  }

  @Provides
  @JvmStatic
  fun strings(): Strings.Editor = SharedLocalizationComponent.strings().editor
}