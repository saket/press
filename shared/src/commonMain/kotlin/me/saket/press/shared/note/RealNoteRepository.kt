package me.saket.press.shared.note

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.asCompletable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.scheduler.Scheduler
import com.benasher44.uuid.Uuid
import me.saket.press.data.shared.Note
import me.saket.press.data.shared.NoteQueries
import me.saket.press.shared.db.DateTimeAdapter
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

  override fun note(noteUuid: Uuid): Observable<Optional<Note>> {
    return noteQueries.note(noteUuid)
        .asObservable(ioScheduler)
        .mapToOneOrOptional()
  }

  override fun notes(): Observable<List<Note>> {
    return noteQueries.notes()
        .asObservable(ioScheduler)
        .mapToList()
  }

  override fun create(vararg insertNotes: InsertNote): Completable {
    return completableFromFunction {
      noteQueries.transaction {
        for (note in insertNotes) {
          noteQueries.insert(
              localId = null,
              uuid = note.uuid,
              content = note.content,
              createdAt = clock.nowUtc(),
              updatedAt = clock.nowUtc(),
              archivedAtString = null,
              deletedAtString = null
          )
        }
      }
    }
  }

  override fun update(noteUuid: Uuid, content: String): Completable {
    return note(noteUuid)
        .take(1)
        .mapToSome()
        .filter { note -> note.content.trim() != content.trim() }
        .map {
          noteQueries.updateContent(
              uuid = noteUuid,
              content = content,
              updatedAt = clock.nowUtc()
          )
        }
        .asCompletable()
  }

  override fun markAsDeleted(noteUuid: Uuid): Completable {
    return completableFromFunction {
      noteQueries.markAsDeleted(
          uuid = noteUuid,
          deletedAtString = DateTimeAdapter.encode(clock.nowUtc())
      )
    }
  }

  override fun markAsArchived(noteUuid: Uuid): Completable {
    return completableFromFunction {
      noteQueries.markAsArchived(
          uuid = noteUuid,
          archivedAtString = DateTimeAdapter.encode(clock.nowUtc())
      )
    }
  }
}
