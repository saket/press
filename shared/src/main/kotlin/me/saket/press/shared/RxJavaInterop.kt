package me.saket.press.shared

import com.badoo.reaktive.rxjavainterop.asReaktive
import com.badoo.reaktive.rxjavainterop.asRxJava2
import io.reactivex.disposables.Disposable
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.ui.UiUpdate
import me.saket.press.shared.ui.UiUpdate.UiEffect
import me.saket.press.shared.ui.UiUpdate.UiModel
import io.reactivex.Observable as RxJavaObservable

fun <EV, M, EF> RxJavaObservable<EV>.uiUpdates(
  presenter: Presenter<EV, M, EF>
): RxJavaObservable<UiUpdate<out M, out EF>> {
  val events = asReaktive()
  val updates = presenter.uiUpdates(events)
  return compose { updates.asRxJava2() }
}

fun <M, E> RxJavaObservable<UiUpdate<out M, out E>>.subscribe(
  models: (M) -> Unit,
  effects: (E) -> Unit = { throw AssertionError("View can't handle effects because 'effects' argument is missing") }
): Disposable {
  fun Unit.exhaustive() = this
  return subscribe {
    when (it) {
      is UiModel -> models(it.model)
      is UiEffect -> effects(it.effect)
    }.exhaustive()
  }
}
