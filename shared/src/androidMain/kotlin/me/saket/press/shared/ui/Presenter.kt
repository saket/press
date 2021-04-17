package me.saket.press.shared.ui

import com.badoo.reaktive.rxjavainterop.asRxJava2Observable
import io.reactivex.Observable as RxJavaObservable

fun <EV : Any, M : Any> Presenter<EV, M>.models(): RxJavaObservable<M> {
  return this.models().asRxJava2Observable()
}
