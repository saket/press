package compose.home

import dagger.Module
import dagger.Provides
import me.saket.compose.shared.home.HomePresenter
import me.saket.compose.shared.home.SharedHomeComponent
import me.saket.compose.shared.navigation.Navigator

@Module
object HomeModule {

  @Provides
  @JvmStatic
  fun presenter(): HomePresenter.Factory {
    return object : HomePresenter.Factory {
      override fun create(navigator: Navigator) = SharedHomeComponent.presenter(navigator)
    }
  }
}