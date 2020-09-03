package press

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Looper
import androidx.work.WorkManager
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import me.saket.press.shared.di.SharedComponent
import press.di.AppComponent
import press.home.HomeActivity
import press.sync.BackgroundSyncWorker

abstract class PressApp : Application() {
  companion object {
    lateinit var component: AppComponent
  }

  abstract fun buildDependencyGraph(): AppComponent

  override fun onCreate() {
    super.onCreate()
    component = buildDependencyGraph()
    SharedComponent.initialize(this)
    component.inject(this)

    RxAndroidPlugins.setInitMainThreadSchedulerHandler {
      AndroidSchedulers.from(Looper.getMainLooper(), true)
    }

    BackgroundSyncWorker.schedule(WorkManager.getInstance(this))
    doOnAppResume {
      component.syncCoordinator().trigger()
    }
  }

  private fun doOnAppResume(action: () -> Unit) {
    registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
      var resumedActivities = 0

      override fun onActivityResumed(activity: Activity) {
        if (resumedActivities++ == 0) {
          action()
        }
      }

      override fun onActivityPaused(activity: Activity) {
        resumedActivities--
      }

      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
      override fun onActivityStarted(activity: Activity) = Unit
      override fun onActivityStopped(activity: Activity) = Unit
      override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
      override fun onActivityDestroyed(activity: Activity) = Unit
    })
  }
}
