package me.saket.press.shared.editor.folder

import com.badoo.reaktive.observable.ObservableWrapper
import me.saket.press.PressDatabase
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.ui.Presenter

class MoveToFolderPresenter(
  private val args: Args,
  private val database: PressDatabase
) : Presenter<MoveToFolderModel, MoveToFolderEvent>() {

  override fun models(): ObservableWrapper<MoveToFolderEvent> {
    TODO()
  }

  fun interface Factory {
    fun create(args: Args): MoveToFolderPresenter
  }

  data class Args(
    val noteId: NoteId
  )
}
