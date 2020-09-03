package press

import android.app.Application
import android.os.Looper
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.WorkManager
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import me.saket.press.shared.di.SharedComponent
import press.di.AppComponent
import press.sync.BackgroundSyncWorker

abstract class PressApp : Application(), LifecycleObserver {
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
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
  }

  @OnLifecycleEvent(ON_START)
  fun onAppForegrounded() {
    BackgroundSyncWorker.schedule(WorkManager.getInstance(this))
    component.syncCoordinator().trigger()
  }
}
