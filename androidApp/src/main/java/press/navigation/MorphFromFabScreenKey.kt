package press.navigation

import kotlinx.android.parcel.Parcelize
import me.saket.press.shared.ui.ScreenKey

/**
 * Morphs an incoming [screen] from a FAB in the outgoing
 * screen (and vice versa) through [MorphFromFabScreenTransition].
 */
@Parcelize
data class MorphFromFabScreenKey(val screen: ScreenKey) : DelegatingScreenKey(screen)
