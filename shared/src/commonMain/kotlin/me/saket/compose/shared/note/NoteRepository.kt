package me.saket.compose.shared.note

import com.badoo.reaktive.observable.Observable

interface NoteRepository {

  fun notes(): Observable<List<Note>>
}