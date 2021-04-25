package me.saket.press.shared.editor.folder

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
data class MoveToFolderScreenKey(val noteId: NoteId) : ScreenKey

data class MoveToFolderModel(
  val folderPath: String,
  val errorMessage: String?,
)

sealed class MoveToFolderEvent {
  data class NameTextChanged(val name: String) : MoveToFolderEvent()
  object SubmitClicked : MoveToFolderEvent()
}
