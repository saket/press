package press.navigation

import kotlinx.android.parcel.Parcelize
import me.saket.press.shared.ui.ScreenKey

@Parcelize
data class ExpandableScreenKey<T : ScreenKey>(
  val screen: T,
  val expandingFromItemId: Long
) : ScreenKey
