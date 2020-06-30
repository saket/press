package press.util

import com.soywiz.klock.TimeSpan
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Pauses items from upstream while predicate is true.
 */
fun <T, O> Observable<T>.suspendWhile(predicateArgProvider: Observable<O>, predicate: (O) -> Boolean): Observable<T> {
  return Observables.combineLatest(this, predicateArgProvider)
      .filter { (_, predicateValue) -> predicate(predicateValue).not() }
      .map { (upstreamItem) -> upstreamItem }
}

fun Observables.interval(span: TimeSpan): Observable<Long> {
  return Observable.interval(span.millisecondsLong, MILLISECONDS)
}
