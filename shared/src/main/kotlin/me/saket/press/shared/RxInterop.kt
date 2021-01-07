package me.saket.press.shared

import com.badoo.reaktive.observable.map
import com.badoo.reaktive.rxjavainterop.asRxJava2Observable
import me.saket.press.shared.preferences.Setting
import me.saket.press.shared.util.Optional
import me.saket.press.shared.util.toOptional
import com.badoo.reaktive.observable.Observable as ReaktiveObservable
import io.reactivex.Observable as RxJavaObservable

fun <T : Any> Setting<T>.listen(): RxJavaObservable<Optional<T>> {
  val listen: ReaktiveObservable<T?> = this.listen()
  return listen
    .map { value -> value.toOptional() }
    .asRxJava2Observable()
}
