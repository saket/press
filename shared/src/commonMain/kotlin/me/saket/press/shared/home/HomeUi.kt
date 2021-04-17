package me.saket.press.shared.home

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.db.FolderId
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.editor.PreSavedNoteId
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
data class HomeScreenKey(
  val folder: FolderId?
) : ScreenKey {
  companion object {
    fun root() = HomeScreenKey(folder = null)
    fun isRoot(key: ScreenKey) = key is HomeScreenKey && key.folder == null
  }
}

interface HomeEvent {
  object NewNoteClicked : HomeEvent
  data class SearchTextChanged(val text: String) : HomeEvent
}

data class HomeModel(
  val title: String,
  val rows: List<Row>,
  val searchFieldHint: String
) {
  val notes: List<NoteModel> get() = rows.filterIsInstance<NoteModel>()
  val folders: List<FolderModel> get() = rows.filterIsInstance<FolderModel>()

  sealed interface Row {
    val id: Any
    fun screenKey(): ScreenKey

    override fun equals(other: Any?): Boolean
  }

  data class NoteModel(
    override val id: NoteId,
    val title: String,
    val body: String
  ) : Row {
    override fun screenKey(): ScreenKey {
      return EditorScreenKey(
        ExistingNote(PreSavedNoteId(id))
      )
    }
  }

  data class FolderModel(
    override val id: FolderId,
    val title: String
  ) : Row {
    override fun screenKey(): ScreenKey {
      return HomeScreenKey(folder = id)
    }
  }
}
