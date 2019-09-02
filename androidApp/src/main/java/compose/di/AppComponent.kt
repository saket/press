package compose.di

import compose.home.HomeModule
import compose.home.HomeView
import dagger.Component

@Component(modules = [HomeModule::class])
interface AppComponent {
  fun inject(target: HomeView)
}