package compose.util

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observable

fun AppCompatActivity.onDestroys(): Observable<Unit> =
  Observable.create { emitter ->
    lifecycle.addObserver(object : LifecycleObserver {
      @OnLifecycleEvent(ON_DESTROY)
      fun onDestroy() {
        emitter.onNext(Unit)
      }
    })
  }