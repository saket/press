package compose.home

import compose.theme.AppTheme
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import me.saket.compose.shared.home.HomeKoinModule
import me.saket.compose.shared.home.HomePresenter

@Module
object HomeModule {

  @Provides
  @JvmStatic
  fun presenter(): HomePresenter =
    HomeKoinModule.presenter()

  @Provides
  @JvmStatic
  fun style(theme: Observable<AppTheme>): Observable<HomeStyle> =
    theme.map { HomeStyle(it) }
}