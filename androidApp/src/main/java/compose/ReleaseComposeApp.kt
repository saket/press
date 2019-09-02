package compose

import compose.di.AppComponent
import compose.di.DaggerAppComponent
import compose.home.HomeModule

@Suppress("unused")
class ReleaseComposeApp : ComposeApp() {

  override fun buildDependencyGraph(): AppComponent =
    DaggerAppComponent.builder()
        .homeModule(HomeModule)
        .build()
}