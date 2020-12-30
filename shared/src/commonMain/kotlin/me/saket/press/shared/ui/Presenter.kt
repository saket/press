package me.saket.press.shared.ui

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.observableOfEmpty
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.refCount
import com.badoo.reaktive.observable.wrap
import com.badoo.reaktive.subject.unicast.UnicastSubject

/**
 * @param [Event] UI events being performed by the user.
 * @param [Model] Content model for describing the UI
 * @param [Effect] One-off updates on the UI that cannot be modeled as state in the content
 *                 model. For e.g., updating a text field just once, showing a toast or
 *                 navigating to a new screen.
 */
abstract class Presenter<Event : Any, Model : Any, Effect : Any> {
  private val viewEvents = UnicastSubject<Event>()
  private val sharedViewEvents = viewEvents.publish().refCount()

  protected fun viewEvents(): Observable<Event> {
    return sharedViewEvents
  }

  fun dispatch(event: Event) {
    viewEvents.onNext(event)
  }

  /**
   * todo: get rid of this and use null on SwiftUI.
   * Used only by SwiftUI right now. Rendering of Android
   * layouts are delayed until [models] emits a value.
   */
  abstract fun defaultUiModel(): Model

  internal abstract fun models(): ObservableWrapper<Model>

  internal open fun uiEffects(): ObservableWrapper<Effect> = observableOfEmpty<Effect>().wrap()
}

sealed class UiUpdate<Model, Effect> {
  data class UiModel<T>(val model: T) : UiUpdate<T, Nothing>()
  data class UiEffect<T>(val effect: T) : UiUpdate<Nothing, T>()
}
