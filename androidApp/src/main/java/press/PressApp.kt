package press

import android.app.Application
import android.os.Looper
import com.soywiz.klock.seconds
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers.computation
import io.reactivex.schedulers.Schedulers.io
import me.saket.press.shared.di.SharedComponent
import me.saket.press.shared.sync.Syncer
import me.saket.press.shared.sync.git.syncRx2
import press.di.AppComponent
import press.extensions.interval
import timber.log.Timber
import javax.inject.Inject

abstract class PressApp : Application() {
  companion object {
    lateinit var component: AppComponent
  }

  @Inject lateinit var syncer: Syncer
  private lateinit var syncDisposable: Disposable

  override fun onCreate() {
    super.onCreate()
    component = buildDependencyGraph()
    SharedComponent.initialize(this)
    component.inject(this)

    RxAndroidPlugins.setInitMainThreadSchedulerHandler {
      AndroidSchedulers.from(Looper.getMainLooper(), true)
    }

    // todo: use workmanager to schedule syncing in the background.
    syncDisposable = Observables.interval(30.seconds, initial = 0)
        .flatMapCompletable {
          syncer.syncRx2()
              .doOnError { e -> Timber.e(e, "Failed to sync notes") }
              .onErrorComplete()
        }
        .subscribe()
  }

  abstract fun buildDependencyGraph(): AppComponent
}
