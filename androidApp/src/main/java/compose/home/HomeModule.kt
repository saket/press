package compose.home

import com.squareup.inject.assisted.dagger2.AssistedModule
import compose.theme.AppTheme
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import me.saket.compose.shared.home.HomeKoinModule
import me.saket.compose.shared.home.HomePresenter

@AssistedModule
@Module(includes = [AssistedInject_HomeModule::class])
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