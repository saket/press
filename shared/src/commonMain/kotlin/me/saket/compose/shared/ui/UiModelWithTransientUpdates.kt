package me.saket.compose.shared.ui

import com.badoo.reaktive.observable.Observable

interface UiModelWithTransientUpdates<T> {
  val transientUpdates: Observable<T>
}