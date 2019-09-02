package me.saket.compose.shared

import com.badoo.reaktive.observable.Observable

interface Presenter<UiEvent, UiModel> {
  fun contentModels(events: Observable<UiEvent>): Observable<UiModel>
}
