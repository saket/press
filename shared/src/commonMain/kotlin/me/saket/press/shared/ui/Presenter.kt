package me.saket.press.shared.ui

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOfEmpty

interface Presenter<UiEvent, UiModel, UiUpdate> {
  fun uiModels(events: Observable<UiEvent>): Observable<UiModel>
  fun uiUpdates(): Observable<UiUpdate> = observableOfEmpty()
}
