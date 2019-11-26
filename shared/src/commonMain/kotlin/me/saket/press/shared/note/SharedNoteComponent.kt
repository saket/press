package me.saket.press.shared.note

import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object SharedNoteComponent {
  val module = module {
    factory<NoteRepository> { RealNotesRepository(get(), get(named("io")), get()) }
  }
}
