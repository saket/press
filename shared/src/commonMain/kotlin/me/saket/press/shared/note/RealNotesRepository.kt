package me.saket.press.shared.note

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.scheduler.Scheduler
import com.benasher44.uuid.Uuid
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import me.saket.press.data.shared.Note
import me.saket.press.data.shared.NoteQueries
import me.saket.press.shared.rx.asObservable
import me.saket.press.shared.rx.mapToList
import me.saket.press.shared.rx.mapToOneOrOptional
import me.saket.press.shared.time.Clock
import me.saket.press.shared.util.Optional

internal class RealNotesRepository(
  private val noteQueries: NoteQueries,
  private val settings: ObservableSettings,
  private val ioScheduler: Scheduler,
  private val clock: Clock
) : NoteRepository {

  init {
    val welcomeNotesPopulated = settings.get("prepopulated_notes_inserted", defaultValue = false)
    if (welcomeNotesPopulated.not()) {
      create(*PrePopulatedNotes.ALL)
          .subscribeOn(ioScheduler)
          .subscribe {
            settings["prepopulated_notes_inserted"] = true
          }
    }
  }

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
    return create(InsertNote(noteUuid, content))
  }

  private fun create(vararg insertNotes: InsertNote): Completable {
    return completableFromFunction {
      noteQueries.transaction {
        for (note in insertNotes) {
          noteQueries.insert(
              localId = null,
              uuid = note.uuid,
              content = note.content,
              createdAt = clock.nowUtc(),
              updatedAt = clock.nowUtc(),
              deletedAt = null
          )
        }
      }
    }
  }

  override fun update(noteUuid: Uuid, content: String): Completable {
    return completableFromFunction {
      noteQueries.updateContent(
          uuid = noteUuid,
          content = content
      )
    }
  }

  override fun markAsDeleted(noteUuid: Uuid): Completable {
    return completableFromFunction {
      noteQueries.markAsDeleted(
          uuid = noteUuid,
          deletedAt = clock.nowUtc()
      )
    }
  }
}
