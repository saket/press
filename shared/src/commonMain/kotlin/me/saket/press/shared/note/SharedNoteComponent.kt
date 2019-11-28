package me.saket.press.shared.note

import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object SharedNoteComponent {
  val module = module {
    single<NoteRepository> { RealNoteRepository(
        noteQueries = get(),
        ioScheduler = get(named("io")),
        clock = get()
    ) }
    single {
      PrePopulatedNotes(
          settings = get(),
          repository = get(),
          ioScheduler = get(named("io"))
      )
    }
  }
}
