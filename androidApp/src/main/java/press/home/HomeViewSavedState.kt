package press.home

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HomeViewSavedState(
  val superState: Parcelable?
) : Parcelable
