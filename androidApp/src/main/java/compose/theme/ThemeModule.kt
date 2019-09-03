package compose.theme

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import me.saket.compose.shared.theme.DraculaThemePalette
import me.saket.compose.shared.theme.ThemePalette

@Module
object ThemeModule {

  @Provides
  @JvmStatic
  fun palette(): Observable<ThemePalette> =
    Observable.just(DraculaThemePalette)
}