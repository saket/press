package me.saket.press.shared.rx

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.observableInterval
import com.badoo.reaktive.scheduler.Scheduler
import com.soywiz.klock.TimeSpan
import me.saket.press.shared.util.Optional
import me.saket.press.shared.util.toOptional

internal fun <T, R : Any> Observable<T>.mapToOptional(mapper: (T) -> R?): Observable<Optional<R>> {
  return map { mapper(it).toOptional() }
}

internal fun <T : Any> Observable<Optional<T>>.mapToSome(): Observable<T> {
  return map { (item) -> item!! }
}

internal fun observableInterval(interval: TimeSpan, scheduler: Scheduler): Observable<Long> {
  return observableInterval(interval.milliseconds.toLong(), scheduler)
}
