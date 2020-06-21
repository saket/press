package me.saket.press.shared.note

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.asCompletable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.scheduler.Scheduler
import me.saket.press.data.shared.Note
import me.saket.press.data.shared.NoteQueries
import me.saket.press.shared.db.DateTimeAdapter
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.rx.asObservable
import me.saket.press.shared.rx.mapToList
import me.saket.press.shared.rx.mapToOneOrOptional
import me.saket.press.shared.rx.mapToSome
import me.saket.press.shared.time.Clock
import me.saket.press.shared.util.Optional

internal class RealNoteRepository(
  private val noteQueries: NoteQueries,
  private val ioScheduler: Scheduler,
  private val clock: Clock
) : NoteRepository {

  override fun note(id: NoteId): Observable<Optional<Note>> {
    return noteQueries.note(id)
        .asObservable(ioScheduler)
        .mapToOneOrOptional()
  }

  override fun visibleNotes(): Observable<List<Note>> {
    return noteQueries.visibleNotes()
        .asObservable(ioScheduler)
        .mapToList()
  }

  override fun create(vararg insertNotes: InsertNote): Completable {
    return completableFromFunction {
      noteQueries.transaction {
        for (note in insertNotes) {
          noteQueries.insert(
              uuid = note.id,
              content = note.content,
              createdAt = note.createdAt ?: clock.nowUtc(),
              updatedAt = note.createdAt ?: clock.nowUtc()
          )
        }
      }
    }
  }

  override fun update(id: NoteId, content: String): Completable {
    return note(id)
        .take(1)
        .mapToSome()
        .filter { note -> note.content.trim() != content.trim() }
        .map {
          noteQueries.updateContent(
              uuid = id,
              content = content,
              updatedAt = clock.nowUtc()
          )
        }
        .asCompletable()
  }

  override fun markAsPendingDeletion(id: NoteId): Completable {
    return completableFromFunction {
      noteQueries.markAsPendingDeletion(
          uuid = id,
          deletedAtString = DateTimeAdapter.encode(clock.nowUtc())
      )
    }
  }

  override fun markAsArchived(id: NoteId): Completable {
    return completableFromFunction {
      noteQueries.markAsArchived(
          uuid = id,
          archivedAtString = DateTimeAdapter.encode(clock.nowUtc())
      )
    }
  }
}
