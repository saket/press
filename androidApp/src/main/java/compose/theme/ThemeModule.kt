package compose.theme

import dagger.Module
import dagger.Provides
import io.reactivex.Observable

@Module
object ThemeModule {

  @Provides
  @JvmStatic
  fun palette(): Observable<ThemePalette> =
    Observable.just(DraculaThemePalette)
}