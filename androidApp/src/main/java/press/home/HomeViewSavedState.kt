package press.home

import android.os.Parcelable
import com.benasher44.uuid.Uuid
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.WriteWith
import me.saket.press.shared.home.HomeUiModel
import press.db.UuidParceler

@Parcelize
data class HomeViewSavedState(
  val superState: Parcelable?,
  val activeNote: ActiveNote?
) : Parcelable

@Parcelize
data class ActiveNote(
  val noteUuid: @WriteWith<UuidParceler> Uuid,
  val adapterId: Long
) : Parcelable

fun HomeUiModel.Note.toActiveNote() = ActiveNote(noteUuid, adapterId)
