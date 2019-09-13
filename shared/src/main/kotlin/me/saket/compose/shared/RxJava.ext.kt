package me.saket.compose.shared

import com.badoo.reaktive.rxjavainterop.toReaktive
import com.badoo.reaktive.rxjavainterop.toRxJava2
import me.saket.compose.shared.ui.Presenter
import io.reactivex.Observable as RxJavaObservable

fun <E, M> RxJavaObservable<E>.uiModels(presenter: Presenter<E, M, out Any>): RxJavaObservable<M> {
  val events = toReaktive()
  val models = presenter.uiModels(events)
  return compose { models.toRxJava2() }
}

// TODO: Find a way to remove 2 from the name.
fun <U> Presenter<out Any, out Any, U>.uiUpdates2(): RxJavaObservable<U> {
  return uiUpdates().toRxJava2()
}