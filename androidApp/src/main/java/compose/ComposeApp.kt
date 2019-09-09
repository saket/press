package compose

import android.app.Application
import android.os.Looper
import compose.di.AppComponent
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import me.saket.compose.shared.di.SharedAppComponent

abstract class ComposeApp : Application() {

  companion object {
    lateinit var component: AppComponent
  }

  override fun onCreate() {
    super.onCreate()
    component = buildDependencyGraph()
    SharedAppComponent.initialize(this)

    RxAndroidPlugins.setInitMainThreadSchedulerHandler {
      AndroidSchedulers.from(Looper.getMainLooper(), true)
    }
  }

  abstract fun buildDependencyGraph(): AppComponent

}