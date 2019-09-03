package me.saket.compose.shared.util

import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.asCompletable
import com.badoo.reaktive.observable.doOnBeforeNext

inline fun <T : Any, R : Any> Observable<T>.consumeOnNext(
  crossinline sideEffect: (T) -> Unit
): Observable<R> =
  doOnBeforeNext { sideEffect(it) }
      .asCompletable()
      .asObservable()