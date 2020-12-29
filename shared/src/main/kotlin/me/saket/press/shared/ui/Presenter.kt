package me.saket.press.shared.ui

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.rxjavainterop.asRxJava2Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import me.saket.press.shared.ui.UiUpdate.UiEffect
import me.saket.press.shared.ui.UiUpdate.UiModel
import io.reactivex.Observable as RxJavaObservable

fun <EV : Any, M : Any, EF : Any> Presenter<EV, M, EF>.uiUpdates(): RxJavaObservable<UiUpdate<out M, out EF>> {
  return merge(
    uiModels().map(::UiModel),
    uiEffects().map(::UiEffect)
  ).asRxJava2Observable()
}

fun <M, EF> RxJavaObservable<UiUpdate<out M, out EF>>.subscribe(
  models: (M) -> Unit,
  effects: (EF) -> Unit = { throw AssertionError("View can't handle effects because 'effects' argument is missing") }
): Disposable {
  fun Unit.exhaustive() = this
  return subscribe {
    when (it) {
      is UiModel -> models(it.model)
      is UiEffect -> effects(it.effect)
    }.exhaustive()
  }
}

fun <M, EF> RxJavaObservable<UiUpdate<out M, out EF>>.models(): RxJavaObservable<M> {
  return ofType<UiModel<M>>().map { it.model }
}
