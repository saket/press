package me.saket.compose.shared.note

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.scheduler.Scheduler
import me.saket.compose.data.shared.Note
import me.saket.compose.data.shared.NoteQueries
import me.saket.compose.shared.db.asObservable
import me.saket.compose.shared.db.mapToList

internal class RealNotesRepository(
  private val noteQueries: NoteQueries,
  private val ioScheduler: Scheduler
) : NoteRepository {

  override fun notes(): Observable<List<Note>> {
    return noteQueries
        .selectAll()
        .asObservable(ioScheduler)
        .mapToList()
  }
}