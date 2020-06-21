package press.home

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.WriteWith
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.home.HomeUiModel
import press.db.NoteIdParceler

@Parcelize
data class HomeViewSavedState(
  val superState: Parcelable?,
  val activeNote: ActiveNote?
) : Parcelable

@Parcelize
data class ActiveNote(val noteId: @WriteWith<NoteIdParceler> NoteId) : Parcelable

fun HomeUiModel.Note.toActiveNote() = ActiveNote(noteId)
