package me.saket.press.shared.rx

import co.touchlab.stately.concurrency.AtomicBoolean
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableEmitter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.scheduler.Scheduler
import com.squareup.sqldelight.Query

// Copied from
// https://github.com/touchlab/DroidconKotlin/blob/kpg/reaktive/sessionize/lib/src/commonMain/kotlin/co/touchlab/sessionize/reaktive/ReaktiveExtensions.kt

internal fun <T : Any> Query<T>.asObservable(scheduler: Scheduler) = observable<Query<T>> { emitter ->
  val listenerAndDisposable = QueryListenerAndDisposable(emitter, this)
  emitter.setDisposable(listenerAndDisposable)
  this.addListener(listenerAndDisposable)
  emitter.onNext(this)
}.observeOn(scheduler)

private class QueryListenerAndDisposable<T : Any>(
  private val emitter: ObservableEmitter<Query<T>>,
  private val query: Query<T>
) : Query.Listener, Disposable {
  private val ab = AtomicBoolean(false)
  override fun queryResultsChanged() {
    emitter.onNext(query)
  }

  override val isDisposed = ab.value

  override fun dispose() {
    if (ab.compareAndSet(expected = false, new = true)) {
      query.removeListener(this)
    }
  }
}

internal fun <T : Any> Observable<Query<T>>.mapToOne(): Observable<T> {
  return map { it.executeAsOne() }
}

internal fun <T : Any> Observable<Query<T>>.mapToOneOrNull(): Observable<T?> {
  return map { it.executeAsOneOrNull() }
}

internal fun <T : Any> Observable<Query<T>>.mapToList(): Observable<List<T>> {
  return map { it.executeAsList() }
}
