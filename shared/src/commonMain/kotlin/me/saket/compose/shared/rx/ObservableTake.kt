package me.saket.compose.shared.rx

import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.utils.atomic.AtomicInt

/**
 * Emit only the first [limit] items emitted by source.
 */
fun <T> Observable<T>.take(limit: Int): Observable<T> {
  require(limit > 0) { "count > 0 required but it was $limit" }

  return observable { emitter ->
    val disposableWrapper = DisposableWrapper()
    emitter.setDisposable(disposableWrapper)

    val remaining = AtomicInt(limit)

    subscribe(object : ObservableObserver<T>, CompletableCallbacks by emitter {
      override fun onNext(value: T) {
        if (remaining.value > 0) {
          val stop = remaining.addAndGet(-1) == 0
          emitter.onNext(value)
          if (stop) {
            onComplete()
          }
        }
      }

      override fun onSubscribe(disposable: Disposable) {
        disposableWrapper.set(disposable)
      }
    })
  }
}