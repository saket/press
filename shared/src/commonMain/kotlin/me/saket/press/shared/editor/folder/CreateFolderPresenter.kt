package me.saket.press.shared.editor.folder

import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.wrap
import me.saket.press.PressDatabase
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.syncer.git.FolderPaths
import me.saket.press.shared.ui.Presenter

class CreateFolderPresenter(
  private val args: Args,
  private val database: PressDatabase,
  private val schedulers: Schedulers,
) : Presenter<CreateFolderEvent, CreateFolderModel>() {

  private val folderPaths = FolderPaths(database)

  override fun models(): ObservableWrapper<CreateFolderModel> {
    return viewEvents().publish { events ->
      observableOf(
        CreateFolderModel(
          folderPath = args.screenKey.preFilledFolderPath,
          errorMessage = null
        )
      )
    }.wrap()
  }

  fun interface Factory {
    fun create(args: Args): CreateFolderPresenter
  }

  data class Args(
    val screenKey: CreateFolderScreenKey,
  )
}
