package me.saket.compose.shared.util

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map

fun <T : Any> Observable<Optional<T>>.filterSome() =
  filter { it is Some<T> }
      .map { (it as Some<T>).value }

fun <T : Any> Observable<Optional<T>>.filterNone() =
  filter { it is None }
      .map { None }