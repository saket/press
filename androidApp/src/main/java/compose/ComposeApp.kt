package compose

import android.app.Application
import compose.di.AppComponent
import me.saket.compose.shared.di.SharedAppComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

abstract class ComposeApp : Application() {

  companion object {
    lateinit var component: AppComponent
  }

  override fun onCreate() {
    super.onCreate()
    component = buildDependencyGraph()
    SharedAppComponent.initialize(this)

    // TODO: Move to debug app.
    Timber.plant(DebugTree())
  }

  abstract fun buildDependencyGraph(): AppComponent

}