package me.saket.compose.shared

import com.badoo.reaktive.rxjavainterop.toReaktive
import com.badoo.reaktive.rxjavainterop.toRxJava2
import com.badoo.reaktive.rxjavainterop.toRxJava2Source
import me.saket.compose.shared.ui.UiModelWithTransientUpdates
import io.reactivex.Observable as RxJavaObservable

fun <T, R> RxJavaObservable<T>.contentModels(presenter: Presenter<T, R>): RxJavaObservable<R> {
  val events = toReaktive()
  val models = presenter.contentModels(events)
  return compose { models.toRxJava2() }
}

fun <T: UiModelWithTransientUpdates<R>, R> RxJavaObservable<T>.transientUpdates(): RxJavaObservable<R> {
  return concatMap<R> { it.transientUpdates.toRxJava2Source() }
}