package press

import press.di.AppComponent
import press.di.DaggerAppComponent

@Suppress("unused")
class ReleaseApp : App() {

  override fun buildDependencyGraph(): AppComponent =
    DaggerAppComponent.builder().build()
}