package press.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.View
import flow.Direction.REPLACE
import flow.Flow
import flow.History
import flow.KeyChanger
import flow.KeyDispatcher
import flow.KeyParceler
import kotlinx.android.parcel.Parcelize
import me.saket.press.shared.ui.IntentLauncher
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.ScreenKey
import me.saket.press.shared.ui.ScreenResult
import me.saket.press.shared.ui.ScreenResults
import press.extensions.unsafeLazy
import press.navigation.BackPressInterceptor.InterceptResult.Ignored

/**
 * Maintains a backstack of screens using Square's Flow. Flow may be deprecated, but it's battle-tested
 * and good-enough for this app's needs. Using Jetpack Navigation wasn't an option because it:
 *
 * a) Expects apps to hand-write their navigation graph before-hand.
 * b) Relies on XML and View IDs, and can't entirely be used from Kotlin.
 */
class RealNavigator(
  private val activity: Activity,
  private val keyChanger: ScreenKeyChanger,
  private val screenResults: ScreenResults
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
    activity.runOnUiThread {
      val head = flow.history.top<CompositeScreenKey>()
      if (head.foreground != screen) {
        flow.set(
          CompositeScreenKey(
            background = head.foreground,
            foreground = screen
          )
        )
      }
    }
  }

  override fun splitScreenAndLfg(screen: ScreenKey) {
    activity.startActivity(
      TheActivity.intent(activity, singleTop = false)
        .addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
  }

  @Suppress("NAME_SHADOWING")
  override fun clearTopAndLfg(screen: ScreenKey) {
    check(screen !is CompositeScreenKey)
    flow.setHistory(History.single(CompositeScreenKey(screen)), REPLACE)
  }

  override fun goBack(result: ScreenResult?) {
    activity.runOnUiThread {
      if (keyChanger.onInterceptBackPress() == Ignored) {
        if (!flow.goBack()) {
          activity.finish()
        }
      }

      // Must happen after the back traversal has finished and Views are ready.
      result?.let(screenResults::broadcast)
    }
  }

  override fun intentLauncher(): IntentLauncher {
    return RealIntentLauncher(activity)
  }
}

/** Get the [ScreenKey] that was used for navigating to a screen. */
inline fun <reified T : ScreenKey> View.screenKey(): T {
  return (Flow.getKey<ScreenKey>(this) as? T) ?: error("No key found for ${this::class.simpleName}")
}

/**
 * Square Flow has an annoying requirement of setting it up before onCreate gets called,
 * making it difficult to, say, read intent extras to determine the initial screen.
 * Press provides it with a dummy screen key to begin with and resets the backstack
 * later with an actual screen.
 */
@Parcelize
class PlaceholderScreenKey : ScreenKey
