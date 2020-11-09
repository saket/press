package press.navigation

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import android.view.View
import flow.Flow
import flow.KeyDispatcher
import flow.KeyParceler
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.ScreenKey
import press.extensions.unsafeLazy
import press.navigation.BackPressInterceptor.InterceptResult.Ignored

/**
 * Maintains a backstack of screens using Square's Flow. Flow may be deprecated, but it's battle-tested
 * and good-enough for this app's needs. Using Jetpack Navigation wasn't an option because it:
 *
 * a) Expects apps to hand-write their navigation graph before-hand.
 * b) Relies on XML and View IDs, and can't entirely be used from Kotlin.
 */
class RealNavigator constructor(
  private val activity: Activity,
  private val keyChanger: ScreenKeyChanger
) : Navigator {
  private val flow by unsafeLazy { Flow.get(activity) }

  fun installInContext(baseContext: Context, initialScreen: ScreenKey): Context {
    val keyDispatcher = KeyDispatcher.configure(activity, keyChanger).build()
    val keyParceler = object : KeyParceler {
      override fun toParcelable(key: Any) = key as Parcelable
      override fun toKey(parcelable: Parcelable) = parcelable
    }

    return Flow.configure(baseContext, activity)
      .defaultKey(initialScreen)
      .dispatcher(keyDispatcher)
      .keyParceler(keyParceler)
      .install()
  }

  // https://www.urbandictionary.com/define.php?term=lfg
  override fun lfg(screen: ScreenKey) {
    flow.set(screen)
  }

  override fun goBack(): Boolean {
    return when (keyChanger.onInterceptBackPress()) {
      Ignored -> flow.goBack()
      else -> false
    }
  }
}

/** Get the [ScreenKey] that was used for navigating to a screen. */
fun <T : ScreenKey> View.screenKey(): T {
  return Flow.getKey<T>(this) ?: error("No key found for ${this::class.java.simpleName}")
}
