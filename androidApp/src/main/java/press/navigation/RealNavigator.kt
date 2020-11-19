package press.navigation

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import android.view.View
import flow.Direction.REPLACE
import flow.Flow
import flow.History
import flow.KeyDispatcher
import flow.KeyParceler
import kotlinx.android.parcel.Parcelize
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
    activity.runOnUiThread {
      flow.set(
        CompositeScreenKey(
          background = flow.history.top<CompositeScreenKey>().foreground,
          foreground = screen
        )
      )
    }
  }

  @Suppress("NAME_SHADOWING")
  override fun clearTopAndLfg(screen: ScreenKey) {
    activity.runOnUiThread {
      val screen = if (screen is CompositeScreenKey) screen else CompositeScreenKey(screen)
      flow.setHistory(History.single(screen), REPLACE)
    }
  }

  override fun goBack(otherwise: (() -> Unit)?) {
    activity.runOnUiThread {
      if (keyChanger.onInterceptBackPress() == Ignored) {
        if (!flow.goBack()) {
          otherwise?.invoke()
        }
      }
    }
  }
}

/** Get the [ScreenKey] that was used for navigating to a screen. */
inline fun <reified T : ScreenKey> View.screenKey(): T {
  val key = Flow.getKey<ScreenKey>(this) ?: error("No key found for ${this::class.simpleName}")
  return key.unwrapDelegate() as T
}

fun ScreenKey.unwrapDelegate(): ScreenKey {
  return when (this) {
    is DelegatingScreenKey -> delegate.unwrapDelegate()
    else -> this
  }
}

abstract class DelegatingScreenKey(val delegate: ScreenKey) : ScreenKey {
  open fun transformDelegateView(view: View) = view
}

/**
 * Square Flow has an annoying requirement of setting it up before onCreate gets called,
 * making it difficult to, say, read intent extras to determine the initial screen.
 * Press provides it with a dummy screen key to begin with and resets the backstack
 * later with an actual screen.
 */
@Parcelize
class PlaceholderScreenKey : ScreenKey
