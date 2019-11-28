package me.saket.press.shared.note

import me.saket.press.shared.settings.customTypeSetting
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object SharedNoteComponent {
  val module = module {
    single<NoteRepository> {
      RealNoteRepository(
          noteQueries = get(),
          ioScheduler = get(named("io")),
          clock = get()
      )
    }
    single {
      PrePopulatedNotes(
          setting = get(),
          repository = get(),
          ioScheduler = get(named("io"))
      )
    }
    factory {
      customTypeSetting(
          settings = get(),
          key = "prepopulated_notes_inserted",
          from = { PrePopulatedNotesInserted(it.toBoolean()) },
          to = { it.inserted.toString() },
          defaultValue = PrePopulatedNotesInserted(false)
      )
    }
  }
}
