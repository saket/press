package me.saket.press.shared.theme

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.behavior.BehaviorSubject

class AppTheme(default: ThemePalette) {
  private val stream = BehaviorSubject(default)
  val palette get() = stream.value

  fun change(palette: ThemePalette) {
    stream.onNext(palette)
  }

  internal fun listen(): Observable<ThemePalette> = stream
}
