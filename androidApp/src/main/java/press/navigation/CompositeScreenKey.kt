package press.navigation

import flow.MultiKey
import kotlinx.android.parcel.Parcelize
import me.saket.press.shared.ui.ScreenKey

/**
 * For showing screens with transparent backgrounds, Press keeps two screens active at all times.
 * This enables pull-to-collapse, and may also work well for dialogs & floating windows in the future.
 */
@Parcelize
data class CompositeScreenKey(
  val foreground: ScreenKey,
  val background: ScreenKey? = null
) : DelegatingScreenKey(delegate = foreground), MultiKey {

  override fun getKeys(): MutableList<Any> {
    return listOfNotNull(background, foreground).toMutableList()
  }
}
