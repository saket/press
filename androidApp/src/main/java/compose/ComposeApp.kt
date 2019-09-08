package compose

import android.app.Application
import android.os.Looper
import compose.di.AppComponent
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
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

    RxAndroidPlugins.setInitMainThreadSchedulerHandler {
      AndroidSchedulers.from(Looper.getMainLooper(), true)
    }
  }

  abstract fun buildDependencyGraph(): AppComponent

}