package me.saket.press.shared.db

import com.badoo.reaktive.scheduler.computationScheduler
import com.badoo.reaktive.scheduler.ioScheduler
import com.squareup.sqldelight.db.SqlDriver
import me.saket.press.PressDatabase
import me.saket.press.data.shared.Note
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object SharedDatabaseComponent {
  val module = module {
    single { get<SqlDriver>().createPressDatabase() }
    single { get<PressDatabase>().noteQueries }
    single(named("io")) { ioScheduler }
    single(named("computation")) { computationScheduler }
  }
}

internal fun SqlDriver.createPressDatabase(): PressDatabase {
  return PressDatabase(
      driver = this,
      noteAdapter = Note.Adapter(
          uuidAdapter = UuidAdapter(),
          createdAtAdapter = DateTimeAdapter,
          updatedAtAdapter = DateTimeAdapter
      )
  )
}
