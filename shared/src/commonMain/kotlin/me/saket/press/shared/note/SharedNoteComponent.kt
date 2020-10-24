package me.saket.press.shared.note

import me.saket.press.shared.settings.Setting
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal class SharedNoteComponent {
  val module = module {
    single<NoteRepository> {
      RealNoteRepository(get(), get(), get())
    }
    single {
      PrePopulatedNotes(
        setting = get(named("prepopulated_notes")),
        repository = get(),
        schedulers = get()
      )
    }
    factory(named("prepopulated_notes")) {
      Setting.create(
        settings = get(),
        key = "prepopulated_notes_inserted",
        from = { PrePopulatedNotesInserted(it.toBoolean()) },
        to = { it.inserted.toString() },
        defaultValue = PrePopulatedNotesInserted(false)
      )
    }
  }
}
