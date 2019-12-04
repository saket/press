package me.saket.press.shared.ui

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOfEmpty
import me.saket.press.shared.ui.UiUpdate.UiModel
import me.saket.press.shared.ui.UiUpdate.UiEffect

/**
 * @param [Event] UI events being performed by the user.
 * @param [Model] Content model for describing the UI
 * @param [Effect] One-off updates on the UI that cannot be stored inside the content model.
 *                 E.g., updating a text field just once, showing a toast or navigating to a
 *                 new screen.
 */
interface Presenter<Event, Model, Effect> {
  fun uiModels(events: Observable<Event>): Observable<Model>
  fun uiEffects(): Observable<Effect> = observableOfEmpty()

  fun uiUpdates(events: Observable<Event>): Observable<UiUpdate<out Model, out Effect>> {
    return merge(
        uiModels(events).map(::UiModel),
        uiEffects().map(::UiEffect)
    )
  }
}

sealed class UiUpdate<Model, Effect> {
  data class UiModel<T>(val model: T) : UiUpdate<T, Nothing>()
  data class UiEffect<T>(val effect: T) : UiUpdate<Nothing, T>()
}

