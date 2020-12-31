package me.saket.press.shared.ui

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.refCount
import com.badoo.reaktive.subject.unicast.UnicastSubject

/**
 * @param [Event] UI events being performed by the user.
 * @param [Model] Content model for describing the UI
 */
abstract class Presenter<Event : Any, Model : Any> {
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
  open fun defaultUiModel(): Model = TODO()

  internal abstract fun models(): ObservableWrapper<Model>
}
