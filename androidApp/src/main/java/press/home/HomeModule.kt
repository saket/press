package press.home

import dagger.Module
import dagger.Provides
import me.saket.press.shared.home.HomePresenter
import me.saket.press.shared.home.SharedHomeComponent

@Module
object HomeModule {
  @Provides
  fun presenter(): HomePresenter.Factory {
    return HomePresenter.Factory { SharedHomeComponent.presenter(it) }
  }
}
