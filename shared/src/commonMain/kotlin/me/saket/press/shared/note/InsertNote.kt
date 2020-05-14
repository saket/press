package me.saket.press.shared.note

import me.saket.press.shared.db.NoteId

data class InsertNote internal constructor(val id: NoteId, val content: String)
