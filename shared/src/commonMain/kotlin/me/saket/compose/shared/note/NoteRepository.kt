package me.saket.compose.shared.note

import com.badoo.reaktive.observable.Observable
import me.saket.compose.data.shared.Note

interface NoteRepository {
  fun notes(): Observable<List<Note>>
}