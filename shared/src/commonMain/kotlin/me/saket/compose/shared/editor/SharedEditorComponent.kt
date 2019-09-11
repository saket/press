package me.saket.compose.shared.editor

import com.benasher44.uuid.Uuid
import me.saket.compose.shared.Strings
import me.saket.compose.shared.di.koin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object SharedEditorComponent {

  val module = module {
    factory { (noteUuid: Uuid) -> EditorPresenter(
        noteUuid = noteUuid,
        noteRepository = get(),
        ioScheduler = get(named("io")),
        strings = get<Strings>().editor
    ) }
  }

  fun presenter(noteUuid: Uuid): EditorPresenter = koin { parametersOf(noteUuid) }
}