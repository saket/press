package me.saket.press.shared.sync

import co.touchlab.stately.concurrency.AtomicInt
import com.soywiz.klock.DateTime
import me.saket.press.PressDatabase
import me.saket.press.data.shared.NoteQueries
import me.saket.press.shared.db.NoteId

class DelegatingPressDatabase(val delegate: PressDatabase) : PressDatabase by delegate {
  private val delegatingNoteQueries = DelegatingNoteQueries(delegate.noteQueries)
  override val noteQueries: DelegatingNoteQueries get() = delegatingNoteQueries
}

class DelegatingNoteQueries(val delegate: NoteQueries) : NoteQueries by delegate {
  val updateCount = AtomicInt(0)

  override fun updateContent(content: String, updatedAt: DateTime, id: NoteId) {
    updateCount.incrementAndGet()
    delegate.updateContent(content, updatedAt, id)
  }
}
