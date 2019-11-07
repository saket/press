package me.saket.compose.shared.note

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.scheduler.Scheduler
import com.benasher44.uuid.Uuid
import me.saket.compose.data.shared.Note
import me.saket.compose.data.shared.NoteQueries
import me.saket.compose.shared.rx.asObservable
import me.saket.compose.shared.rx.mapToList
import me.saket.compose.shared.rx.mapToOneOrOptional
import me.saket.compose.shared.time.Clock
import me.saket.compose.shared.util.Optional

internal class RealNotesRepository(
  private val noteQueries: NoteQueries,
  private val ioScheduler: Scheduler,
  private val clock: Clock
) : NoteRepository {

  override fun note(noteUuid: Uuid): Observable<Optional<Note>> {
    return noteQueries.selectNote(noteUuid)
        .asObservable(ioScheduler)
        .mapToOneOrOptional()
  }

  override fun notes(): Observable<List<Note>> {
    return noteQueries
        .selectAllNonDeleted()
        .asObservable(ioScheduler)
        .mapToList()
  }

  override fun create(noteUuid: Uuid, content: String): Completable {
    return completable {
      noteQueries.insert(
          localId = null,
          uuid = noteUuid,
          content = content,
          createdAt = clock.nowUtc(),
          updatedAt = clock.nowUtc(),
          deletedAt = null
      )
    }
  }

  override fun update(noteUuid: Uuid, content: String): Completable {
    return completable {
      noteQueries.updateContent(
          uuid = noteUuid,
          content = content
      )
    }
  }

  override fun markAsDeleted(noteUuid: Uuid): Completable {
    return completable {
      noteQueries.markAsDeleted(
          uuid = noteUuid,
          deletedAt = clock.nowUtc()
      )
    }
  }
}