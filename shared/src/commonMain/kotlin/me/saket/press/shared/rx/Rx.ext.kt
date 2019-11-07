package me.saket.press.shared.rx

import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.asCompletable
import com.badoo.reaktive.observable.doOnBeforeNext
import com.badoo.reaktive.observable.map
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