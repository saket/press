package me.saket.press.shared.note

import me.saket.press.data.shared.Note
import me.saket.press.shared.db.DateTimeAdapter
import me.saket.press.shared.db.UuidAdapter
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object SharedNoteComponent {
  val module = module {
    factory<NoteRepository> { RealNotesRepository(get(), get(named("io")), get()) }
    single {
      Note.Adapter(
          uuidAdapter = UuidAdapter(),
          createdAtAdapter = DateTimeAdapter(),
          updatedAtAdapter = DateTimeAdapter(),
          deletedAtAdapter = DateTimeAdapter()
      )
    }
  }
}