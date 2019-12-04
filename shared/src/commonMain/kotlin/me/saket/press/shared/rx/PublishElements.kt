package me.saket.press.shared.rx

import com.badoo.reaktive.base.ValueCallback
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.observable.observableUnsafe
import com.badoo.reaktive.observable.publish

/**
 * https://github.com/badoo/Reaktive/issues/315
 */
fun <T, R> Observable<T>.publishElements(block: (Observable<T>) -> Observable<R>): Observable<R> {
  val published = publish()
  return block(published).doOnAfterSubscribe {
    published.connect()
  }
}

fun <T> Observable<T>.doOnAfterSubscribe(action: (Disposable) -> Unit): Observable<T> =
  observableUnsafe { observer ->
    val disposableWrapper = DisposableWrapper()
    observer.onSubscribe(disposableWrapper)

    subscribe(
        object : ObservableObserver<T>, ValueCallback<T> by observer {
          override fun onSubscribe(disposable: Disposable) {
            disposableWrapper.set(disposable)
          }

          override fun onComplete() {
            disposableWrapper.doIfNotDisposed(dispose = true, block = observer::onComplete)
          }

          override fun onError(error: Throwable) {
            disposableWrapper.doIfNotDisposed(dispose = true) {
              observer.onError(error)
            }
          }
        }
    )

    try {
      action(disposableWrapper)
    } catch (e: Throwable) {
      observer.onError(e)
      disposableWrapper.dispose()
    }
  }

internal inline fun Disposable.doIfNotDisposed(dispose: Boolean = false, block: () -> Unit) {
  if (!isDisposed) {
    try {
      block()
    } finally {
      if (dispose) {
        dispose()
      }
    }
  }
}
