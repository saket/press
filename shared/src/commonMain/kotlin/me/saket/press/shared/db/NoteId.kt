package me.saket.press.shared.db

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.squareup.sqldelight.ColumnAdapter

// Inline class would have been nice if Kotlin multi-platform supported it.
data class NoteId(val value: Uuid) {
  class Adapter : ColumnAdapter<NoteId, String> {
    override fun decode(databaseValue: String) = NoteId(uuidFrom(databaseValue))
    override fun encode(value: NoteId) = value.value.toString()
  }

  companion object {
    fun generate() = NoteId(uuid4())
  }
}
