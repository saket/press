package compose

import compose.di.AppComponent
import compose.di.DaggerAppComponent

@Suppress("unused")
class ReleaseComposeApp : ComposeApp() {

  override fun buildDependencyGraph(): AppComponent =
    DaggerAppComponent.builder().build()
}