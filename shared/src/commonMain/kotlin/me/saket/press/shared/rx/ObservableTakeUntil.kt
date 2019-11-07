package me.saket.press.shared.rx

import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableCallbacks
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.observable.serialize

/**
 * TODO: Remove this once its available on Reaktive.
 */
fun <T> Observable<T>.takeUntil(other: Observable<*>): Observable<T> =
  observable {
    val emitter = it.serialize()
    val disposables = CompositeDisposable()
    emitter.setDisposable(disposables)

    val upstreamObserver =
      object : ObservableObserver<T>, ObservableCallbacks<T> by emitter {
        override fun onSubscribe(disposable: Disposable) {
          disposables += disposable
        }
      }

    other.subscribe(
        object : ObservableObserver<Any?>, Observer by upstreamObserver, CompletableCallbacks by upstreamObserver {
          override fun onNext(value: Any?) {
            upstreamObserver.onComplete()
          }
        }
    )

    subscribe(upstreamObserver)
  }