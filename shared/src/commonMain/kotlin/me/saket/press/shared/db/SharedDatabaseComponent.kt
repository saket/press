package me.saket.press.shared.db

import com.badoo.reaktive.scheduler.computationScheduler
import com.badoo.reaktive.scheduler.ioScheduler
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.serialization.json.Json
import me.saket.press.PressDatabase
import me.saket.press.data.shared.Folder
import me.saket.press.data.shared.FolderSyncConfig
import me.saket.press.data.shared.Note
import me.saket.press.shared.sync.git.GitRemoteAndAuth
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal class SharedDatabaseComponent {
  val module = module {
    single { createPressDatabase(get(), get()) }
    single { get<PressDatabase>().noteQueries }
    single(named("io")) { ioScheduler }
    single(named("computation")) { computationScheduler }
  }
}

internal fun createPressDatabase(driver: SqlDriver, json: Json): PressDatabase {
  return PressDatabase(
    driver = driver,
    noteAdapter = Note.Adapter(
      idAdapter = NoteId.SqlAdapter(),
      createdAtAdapter = DateTimeAdapter(),
      updatedAtAdapter = DateTimeAdapter(),
      syncStateAdapter = EnumColumnAdapter(),
      folderIdAdapter = FolderId.SqlAdapter()
    ),
    folderAdapter = Folder.Adapter(
      idAdapter = FolderId.SqlAdapter(),
      parentAdapter = FolderId.SqlAdapter()
    ),
    folderSyncConfigAdapter = FolderSyncConfig.Adapter(
      remoteAdapter = GitRemoteAndAuth.SqlAdapter(json),
      lastSyncedAtAdapter = DateTimeAdapter()
    )
  )
}
