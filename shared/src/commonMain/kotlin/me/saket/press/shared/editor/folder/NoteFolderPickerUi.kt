package me.saket.press.shared.editor.folder

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
data class MoveToFolderScreenKey(val noteId: NoteId) : ScreenKey

class MoveToFolderModel

sealed class MoveToFolderEvent
