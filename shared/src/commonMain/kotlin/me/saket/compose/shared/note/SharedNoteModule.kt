package me.saket.compose.shared.note

import org.koin.dsl.module

object SharedNoteModule {

  val module = module {
    factory<NoteRepository> { RealNotesRepository() }
  }
}