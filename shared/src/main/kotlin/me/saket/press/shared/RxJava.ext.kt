package me.saket.press.shared

import com.badoo.reaktive.rxjavainterop.asReaktive
import com.badoo.reaktive.rxjavainterop.asRxJava2
import me.saket.press.shared.ui.Presenter
import io.reactivex.Observable as RxJavaObservable

fun <E, M> RxJavaObservable<E>.uiModels(presenter: Presenter<E, M, out Any>): RxJavaObservable<M> {
  val events = asReaktive()
  val models = presenter.uiModels(events)
  return compose { models.asRxJava2() }
}

// TODO: Find a way to remove 2 from the name.
fun <U> Presenter<out Any, out Any, U>.uiUpdates2(): RxJavaObservable<U> {
  return uiUpdates().asRxJava2()
}
