package me.saket.press.shared.rx

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.completable.completableTimer
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.asCompletable
import com.badoo.reaktive.observable.combineLatest
import com.badoo.reaktive.observable.delay
import com.badoo.reaktive.observable.doOnBeforeNext
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.observable.observableInterval
import com.badoo.reaktive.observable.startWithValue
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.takeUntil
import com.badoo.reaktive.observable.withLatestFrom
import com.badoo.reaktive.observable.zip
import com.badoo.reaktive.scheduler.Scheduler
import com.soywiz.klock.TimeSpan

internal fun observableInterval(interval: TimeSpan, scheduler: Scheduler): Observable<Long> {
  return observableInterval(interval.milliseconds.toLong(), scheduler)
}

internal fun observableInterval(startDelay: Long, period: TimeSpan, scheduler: Scheduler): Observable<Long> {
  return observableInterval(startDelay, period.millisecondsLong, scheduler)
}

internal fun completableTimer(period: TimeSpan, scheduler: Scheduler): Completable {
  return completableTimer(period.millisecondsLong, scheduler)
}

internal fun <T, O> Observable<T>.withLatestFrom(other: Observable<O>): Observable<Pair<T, O>> {
  return withLatestFrom(other, ::Pair)
}

internal fun <T, O1, O2> Observable<T>.withLatestFrom(
  other1: Observable<O1>,
  other2: Observable<O2>
): Observable<Triple<T, O1, O2>> {
  return withLatestFrom(other1, other2, ::Triple)
}

internal fun <T> Observable<T>.mergeWith(other: Observable<T>): Observable<T> {
  return merge(this, other)
}

internal fun <T> Observable<T>.delay(span: TimeSpan, scheduler: Scheduler): Observable<T> {
  return delay(span.millisecondsLong, scheduler)
}

internal fun <T, R> Observable<T>.consumeOnNext(consume: (T) -> Unit): Observable<R> {
  return doOnBeforeNext { consume(it) }
    .asCompletable()
    .asObservable()
}

internal fun <T> Observable<T?>.filterNull(): Observable<T?> =
  filter { it == null }
    .map { null }

internal fun <T : Any> Observable<T?>.filterNotNull(): Observable<T> =
  notNull()

internal fun <T> Observable<T>.repeatItemWhen(other: Observable<*>): Observable<T> {
  return switchMap { item ->
    other.map { item }.startWithValue(item)
  }
}

internal fun <T, R> Observable<T>.combineLatestWith(other: Observable<R>): Observable<Pair<T, R>> {
  return combineLatest(this, other, ::Pair)
}

internal fun <T, R> Observable<T>.zipWith(other: Observable<R>): Observable<Pair<T, R>> {
  return zip(this, other, ::Pair)
}

internal fun <T, R> zip(first: Observable<T>, second: Observable<R>): Observable<Pair<T, R>> {
  return zip(first, second, ::Pair)
}

fun Completable.takeUntil(until: Observable<*>): Completable {
  return asObservable<Unit>().takeUntil(until).asCompletable()
}
