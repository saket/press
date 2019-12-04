package press

import android.app.Application
import android.os.Looper
import press.di.AppComponent
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import me.saket.press.shared.di.SharedAppComponent

abstract class App : Application() {

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
