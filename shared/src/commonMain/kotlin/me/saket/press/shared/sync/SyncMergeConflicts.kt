package me.saket.press.shared.sync

import com.badoo.reaktive.observable.map
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.sync.git.GitSyncer

/**
 * Holds note IDs that are in a conflicted state right during sync. Notes can remain in a
 * conflicted state temporarily while [GitSyncer] resolves them. This state isn't persisted
 * across process deaths because notes are guaranteed to be conflict-free once the ongoing
 * sync completes successfully. In case sync fails or the app is killed midway, [GitSyncer]
 * discards all upstream changes on the next sync attempt, clearing any dangling merge
 * conflicts.
 *
 * [EditorPresenter] uses this for blocking editing of conflicted notes, asking the user to
 * re-open the note once conflicts have been resolved.
 */
class SyncMergeConflicts {
  private val conflictedNotes = BehaviorSubject<List<NoteId>>(emptyList())

  fun add(noteId: NoteId) =
    conflictedNotes.onNext(conflictedNotes.value + noteId)

  fun clear() =
    conflictedNotes.onNext(emptyList())

  fun isConflicted(noteId: NoteId) =
    conflictedNotes.map { noteId in it }
}
