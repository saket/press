package press.navigation.transitions

import kotlinx.android.parcel.Parcelize
import me.saket.press.shared.ui.ScreenKey
import press.navigation.DelegatingScreenKey

/**
 * Morphs an incoming [screen] from a FAB in the outgoing
 * screen (and vice versa) through [MorphFromFabScreenTransition].
 */
@Parcelize
data class MorphFromFabScreenKey(val screen: ScreenKey) : DelegatingScreenKey(screen)
