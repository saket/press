package compose.home

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.Provides
import me.saket.compose.shared.home.HomePresenter
import me.saket.compose.shared.home.SharedHomeComponent
import me.saket.compose.shared.navigation.Navigator

@AssistedModule
@Module(includes = [AssistedInject_HomeModule::class])
object HomeModule {

  @Provides
  @JvmStatic
  fun presenter(): HomePresenter.Factory {
    return object : HomePresenter.Factory {
      override fun create(navigator: Navigator): HomePresenter {
        return SharedHomeComponent.presenter(navigator)
      }
    }
  }
}