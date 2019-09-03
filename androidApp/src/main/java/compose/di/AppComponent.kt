package compose.di

import compose.home.HomeActivity
import compose.home.HomeView
import compose.widgets.ThemeAwareActivity
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {
  fun inject(target: ThemeAwareActivity)
  fun inject(target: HomeActivity)
  fun inject(target: HomeView)
}