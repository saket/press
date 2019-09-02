package compose

import android.app.Application
import compose.di.AppComponent
import timber.log.Timber

abstract class ComposeApp : Application() {

  companion object {
    lateinit var component: AppComponent
  }

  override fun onCreate() {
    super.onCreate()
    component = buildDependencyGraph()

    // TODO: Move to debug app.
    Timber.plant(Timber.DebugTree())
  }

  abstract fun buildDependencyGraph(): AppComponent

}