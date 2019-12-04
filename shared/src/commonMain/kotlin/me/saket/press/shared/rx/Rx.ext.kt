package me.saket.press.shared.rx

import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.asCompletable
import com.badoo.reaktive.observable.doOnBeforeNext
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.observableInterval
import com.badoo.reaktive.observable.withLatestFrom
import com.badoo.reaktive.scheduler.Scheduler
import com.soywiz.klock.TimeSpan
import me.saket.press.shared.util.Optional
import me.saket.press.shared.util.toOptional

inline fun <T : Any, R : Any> Observable<T>.consumeOnNext(
  crossinline sideEffect: (T) -> Unit
): Observable<R> =
  doOnBeforeNext { sideEffect(it) }
      .asCompletable()
      .asObservable()

fun <T, R : Any> Observable<T>.mapToOptional(mapper: (T) -> R?): Observable<Optional<R>> {
  return map { mapper(it).toOptional() }
}

fun <T, O> Observable<T>.withLatestFrom(other: Observable<O>): Observable<Pair<T, O>> {
  return withLatestFrom(other) { first, second -> first to second }
}

fun <T : Any> Observable<Optional<T>>.mapToSome(): Observable<T> {
  return map { (item) -> item!! }
}

fun observableInterval(interval: TimeSpan, scheduler: Scheduler): Observable<Long> {
  return observableInterval(interval.milliseconds.toLong(), scheduler)
}
