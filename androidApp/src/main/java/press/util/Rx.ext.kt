package press.util

import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables

/**
 * Pauses items from upstream while predicate is true.
 */
fun <T, O> Observable<T>.suspendWhile(predicateArgProvider: Observable<O>, predicate: (O) -> Boolean): Observable<T> {
  return Observables.combineLatest(this, predicateArgProvider)
      .filter { (_, predicateValue) -> predicate(predicateValue).not() }
      .map { (upstreamItem) -> upstreamItem }
}
