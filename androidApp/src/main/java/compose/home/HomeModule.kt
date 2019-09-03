package compose.home

import com.squareup.inject.assisted.dagger2.AssistedModule
import me.saket.compose.shared.theme.ThemePalette
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import me.saket.compose.shared.home.HomeKoinModule
import me.saket.compose.shared.home.HomePresenter
import me.saket.compose.shared.navigation.Navigator

@AssistedModule
@Module(includes = [AssistedInject_HomeModule::class])
object HomeModule {

  @Provides
  @JvmStatic
  fun presenter(): HomePresenter.Factory {
    return object : HomePresenter.Factory {
      override fun create(navigator: Navigator): HomePresenter {
        return HomeKoinModule.presenter(navigator)
      }
    }
  }

  @Provides
  @JvmStatic
  fun style(theme: Observable<ThemePalette>): Observable<HomeStyle> =
    theme.map { HomeStyle(it) }
}