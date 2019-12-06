package press.home

import dagger.Module
import dagger.Provides
import me.saket.press.shared.home.HomePresenter
import me.saket.press.shared.home.SharedHomeComponent

@Module
object HomeModule {

  @Provides
  fun presenter(): HomePresenter.Factory {
    return object : HomePresenter.Factory {
      override fun create(args: HomePresenter.Args) = SharedHomeComponent.presenter(args)
    }
  }
}
