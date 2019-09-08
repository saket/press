package me.saket.compose.shared.editor

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOfEmpty
import me.saket.compose.shared.Presenter

class EditorPresenter : Presenter<EditorEvent, EditorUiModel> {

  override fun contentModels(events: Observable<EditorEvent>): Observable<EditorUiModel> {
    return observableOfEmpty()
  }

  interface Factory {
    fun create(): EditorPresenter
  }
}