package me.saket.compose.shared.editor

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOfEmpty
import me.saket.compose.shared.editor.EditorUiModel.TransientUpdate
import me.saket.compose.shared.ui.UiModelWithTransientUpdates

data class EditorUiModel(
  override val transientUpdates: Observable<TransientUpdate> = observableOfEmpty()
): UiModelWithTransientUpdates<TransientUpdate> {

  sealed class TransientUpdate {
    data class PopulateContent(val content: String) : TransientUpdate()
    object CloseNote : TransientUpdate()
  }
}