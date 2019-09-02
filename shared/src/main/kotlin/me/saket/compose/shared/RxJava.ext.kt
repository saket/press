package me.saket.compose.shared

import com.badoo.reaktive.rxjavainterop.toReaktive
import com.badoo.reaktive.rxjavainterop.toRxJava2
import io.reactivex.Observable

fun <T, R> Observable<T>.contentModels(presenter: Presenter<T, R>): Observable<R> {
  val events = toReaktive()
  val models = presenter.contentModels(events)
  return compose { models.toRxJava2() }
}