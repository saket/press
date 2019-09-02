package compose.home

import dagger.Module
import dagger.Provides
import me.saket.compose.shared.home.HomeKoinModule
import me.saket.compose.shared.home.HomePresenter

@Module
object HomeModule {

  @Provides
  fun presenter(): HomePresenter = HomeKoinModule.presenter()
}