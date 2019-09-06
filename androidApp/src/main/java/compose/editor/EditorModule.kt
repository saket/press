package compose.editor

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import me.saket.compose.shared.theme.ThemePalette

@Module
object EditorModule {

  @Provides
  @JvmStatic
  fun style(theme: Observable<ThemePalette>): Observable<EditorStyle> =
    theme.map { EditorStyle(it) }
}