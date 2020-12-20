package me.saket.press.shared.sync.git

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.value
import com.soywiz.klock.DateTime
import me.saket.press.PressDatabase
import me.saket.press.data.shared.NoteQueries
import me.saket.press.shared.db.NoteId

class DelegatingPressDatabase(val delegate: PressDatabase) : PressDatabase by delegate {
  private val delegatingNoteQueries = DelegatingNoteQueries(delegate.noteQueries)
  override val noteQueries: DelegatingNoteQueries get() = delegatingNoteQueries
}

class DelegatingNoteQueries(val delegate: NoteQueries) : NoteQueries by delegate {
  private val _updateCount = AtomicInt(0)
  val updateCount get() = _updateCount.value

  override fun updateContent(content: String, updatedAt: DateTime, id: NoteId) {
    _updateCount.incrementAndGet()
    delegate.updateContent(content, updatedAt, id)
  }
}
