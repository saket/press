package me.saket.press.shared.sync

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorPresenter

/**
 * Holds information about notes that are in a conflicted state right during sync.
 * [EditorPresenter] uses this for blocking editing of conflicted notes until they're resolved.
 */
class SyncMergeConflicts {
  private val conflictedNotes = BehaviorSubject<List<NoteId>>(emptyList())

  fun add(note: Note) = conflictedNotes.onNext(conflictedNotes.value + note.id)
  fun clear() = conflictedNotes.onNext(emptyList())
  fun listen(): Observable<List<NoteId>> = conflictedNotes
}
