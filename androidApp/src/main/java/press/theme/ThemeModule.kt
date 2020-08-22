package press.theme

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import me.saket.press.shared.theme.DraculaThemePalette
import me.saket.press.shared.theme.ThemePalette

@Module
object ThemeModule {

  @Provides
  fun palette(): Observable<ThemePalette> =
    // todo: share stream
    Observable.just(DraculaThemePalette)
}
