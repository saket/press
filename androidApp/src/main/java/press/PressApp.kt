package press

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Looper
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.WorkManager
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import me.saket.press.R
import me.saket.press.shared.di.SharedComponent
import press.di.AppComponent
import press.navigation.TheActivity

abstract class PressApp : Application(), LifecycleObserver {
  companion object {
    lateinit var component: AppComponent
  }

  abstract fun buildDependencyGraph(): AppComponent

  override fun onCreate() {
    super.onCreate()
    component = buildDependencyGraph()
    SharedComponent.initialize(this)

    RxAndroidPlugins.setInitMainThreadSchedulerHandler {
      AndroidSchedulers.from(Looper.getMainLooper(), true)
    }

    BackgroundSyncWorker.schedule(WorkManager.getInstance(this))
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)

    createLauncherShortcuts()
  }

  /**
   * Press uses dynamic shortcuts instead of static shortcuts so that
   * [TheActivity] can be opened in "singleTask" launch mode.
   */
  private fun createLauncherShortcuts() {
    val newNote = ShortcutInfo.Builder(this, "new_note")
      .setShortLabel(getString(R.string.shortcut_new_note_label))
      .setIcon(Icon.createWithResource(this, R.drawable.ic_note_add_black_24dp))
      .setIntent(TheActivity.intent(this).setAction(Intent.ACTION_SEND))
      .build()

    getSystemService<ShortcutManager>()!!.dynamicShortcuts = listOf(newNote)
  }

  @OnLifecycleEvent(ON_START)
  fun onAppForegrounded() {
    component.syncCoordinator().trigger()
  }

  @OnLifecycleEvent(ON_STOP)
  fun onAppBackgrounded() {
    component.syncCoordinator().trigger()
  }
}
