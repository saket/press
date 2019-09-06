package me.saket.compose.shared.note

import me.saket.compose.data.shared.Note
import me.saket.compose.shared.db.DateTimeTzAdapter
import me.saket.compose.shared.db.UuidAdapter
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object SharedNoteComponent {
  val module = module {
    factory<NoteRepository> { RealNotesRepository(get(), get(named("io"))) }
    single {
      Note.Adapter(
          idAdapter = UuidAdapter(),
          createdAtAdapter = DateTimeTzAdapter(),
          updatedAtAdapter = DateTimeTzAdapter(),
          deletedAtAdapter = DateTimeTzAdapter()
      )
    }
  }
}