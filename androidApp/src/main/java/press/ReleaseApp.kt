package press

import android.annotation.SuppressLint
import press.di.AppComponent
import press.di.DaggerAppComponent

@Suppress("unused")
@SuppressLint("Registered")
class ReleaseApp : App() {

  override fun buildDependencyGraph(): AppComponent =
    DaggerAppComponent.builder().build()
}
