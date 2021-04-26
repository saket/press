package me.saket.press.shared.editor.folder

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
data class CreateFolderScreenKey(val noteId: NoteId) : ScreenKey

data class CreateFolderModel(
  val folderPath: String,
  val errorMessage: String?,
)

sealed class CreateFolderEvent {
  data class NameTextChanged(val name: String) : CreateFolderEvent()
  object SubmitClicked : CreateFolderEvent()
}
