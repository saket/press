package me.saket.press.shared.editor.folder

import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.observable.wrap
import me.saket.press.PressDatabase
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.asObservable
import me.saket.press.shared.rx.mapToOne
import me.saket.press.shared.syncer.git.FolderPaths
import me.saket.press.shared.ui.Presenter

class MoveToFolderPresenter(
  private val args: Args,
  private val database: PressDatabase,
  private val schedulers: Schedulers,
) : Presenter<MoveToFolderEvent, MoveToFolderModel>() {

  private val folderPaths = FolderPaths(database)

  override fun models(): ObservableWrapper<MoveToFolderModel> {
    return viewEvents().publish { events ->
      database.noteQueries.note(args.screenKey.noteId)
        .asObservable(schedulers.io)
        .mapToOne()
        .take(1)
        .map { note ->
          val folderPath = folderPaths.createFlatPath(id = note.folderId)
          MoveToFolderModel(
            folderPath = folderPath,
            errorMessage = null
          )
        }
    }.wrap()
  }

  fun interface Factory {
    fun create(args: Args): MoveToFolderPresenter
  }

  data class Args(
    val screenKey: MoveToFolderScreenKey,
  )
}
