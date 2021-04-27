package me.saket.press.shared.editor.folder

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.ui.HighlightedText
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
data class CreateFolderScreenKey(
  val preFilledFolderPath: String,
  val includeNoteIds: List<NoteId>
) : ScreenKey

data class CreateFolderModel(
  val errorMessage: String?,
  val suggestions: List<FolderSuggestionModel>
)

inline class FolderSuggestionModel(
  val name: HighlightedText
)

sealed class CreateFolderEvent {
  data class FolderPathTextChanged(val path: String) : CreateFolderEvent()
  object SubmitClicked : CreateFolderEvent()
}
