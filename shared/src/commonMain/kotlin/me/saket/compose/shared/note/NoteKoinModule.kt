package me.saket.compose.shared.note

import org.koin.dsl.module

object NoteKoinModule {

  val module = module {
    factory<NoteRepository> { RealNotesRepository() }
  }
}