package me.saket.press.shared.rx

import com.badoo.reaktive.base.tryCatch
import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.observable.observable

/**
 * Emit items by upstream as long as `predicate` is satisfied,
 * and then completes as soon as this condition is not satisfied.
 */
fun <T> Observable<T>.takeWhile(predicate: (T) -> Boolean): Observable<T> =
  observable { emitter ->
    val disposableWrapper = DisposableWrapper()
    emitter.setDisposable(disposableWrapper)

    subscribe(
        object : ObservableObserver<T>, CompletableCallbacks by emitter {
          override fun onSubscribe(disposable: Disposable) {
            disposableWrapper.set(disposable)
          }

          override fun onNext(value: T) {
            emitter.tryCatch(block = { predicate(value) }) {
              when {
                it -> emitter.onNext(value)
                else -> emitter.onComplete()
              }
            }
          }
        }
    )
  }
