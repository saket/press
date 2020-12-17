package me.saket.press.shared.note

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.combineLatest
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.observableFromFunction
import me.saket.press.PressDatabase
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.asObservable
import me.saket.press.shared.rx.mapToList
import me.saket.press.shared.rx.mapToOneOrOptional
import me.saket.press.shared.sync.git.FolderPaths
import me.saket.press.shared.time.Clock
import me.saket.press.shared.util.Optional

internal class RealNoteRepository(
  private val database: PressDatabase,
  private val schedulers: Schedulers,
  private val clock: Clock
) : NoteRepository {
  private val noteQueries get() = database.noteQueries

  override fun note(id: NoteId): Observable<Optional<Note>> {
    return noteQueries.note(id)
      .asObservable(schedulers.io)
      .mapToOneOrOptional()
  }

  override fun visibleNotes(): Observable<List<Note>> {
    return noteQueries.visibleNotes()
      .asObservable(schedulers.io)
      .mapToList()
  }

  override fun create(vararg insertNotes: InsertNote): Completable {
    return completableFromFunction {
      noteQueries.transaction {
        for (note in insertNotes) {
          noteQueries.insert(
            id = note.id,
            folderId = null,
            content = note.content,
            createdAt = note.createdAt ?: clock.nowUtc(),
            updatedAt = note.createdAt ?: clock.nowUtc()
          )
        }
      }
    }
  }

  override fun update(id: NoteId, content: String): Completable {
    return completableFromFunction {
      noteQueries.updateContent(
        id = id,
        content = content,
        updatedAt = clock.nowUtc()
      )
    }
  }

  override fun markAsPendingDeletion(id: NoteId): Completable {
    return completableFromFunction {
      noteQueries.markAsPendingDeletion(id)
    }
  }
}
