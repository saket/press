package me.saket.compose.shared.rx

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.observable.observable

/**
 * TODO: Remove this once its upstreamed to Reaktive.
 * Emit only the first [limit] items emitted by source.
 */
fun <T> Observable<T>.take(limit: Int): Observable<T> {
  require(limit > 0) { "count >= 0 required but it was $limit" }

  return observable { emitter ->
    var remaining = limit

    subscribe(object : ObservableObserver<T> {
      override fun onComplete() {
        emitter.onComplete()
      }

      override fun onError(error: Throwable) {
        emitter.onError(error)
      }

      override fun onNext(value: T) {
        emitter.onNext(value)

        if (--remaining == 0) {
          onComplete()
        }
      }

      override fun onSubscribe(disposable: Disposable) {
        emitter.setDisposable(disposable)
      }
    })
  }
}