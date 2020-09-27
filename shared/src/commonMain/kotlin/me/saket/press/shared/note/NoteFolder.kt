package me.saket.press.shared.note

import com.squareup.sqldelight.ColumnAdapter

inline class NoteFolder(val name: String) {
  object SqlAdapter : ColumnAdapter<NoteFolder, String> {
    override fun decode(databaseValue: String) = NoteFolder(databaseValue)
    override fun encode(value: NoteFolder) = value.name
  }
}
