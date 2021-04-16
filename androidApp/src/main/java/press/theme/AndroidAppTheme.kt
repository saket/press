package press.theme

import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.View
import com.jakewharton.rxbinding3.view.attaches
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Single
import me.saket.press.BuildConfig
import me.saket.press.shared.listenRx
import me.saket.press.shared.preferences.UserPreferences
import me.saket.press.shared.theme.AppTheme
import me.saket.press.shared.theme.ThemePalette
import press.PressApp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAppTheme @Inject constructor(
  app: Application,
  userPreferences: UserPreferences
) : AppTheme(userPreferences, startWithDarkMode = app.resources.configuration.isDarkModeEnabled) {

  init {
    app.registerComponentCallbacks(object : ComponentCallbacks {
      override fun onLowMemory() = Unit
      override fun onConfigurationChanged(newConfig: Configuration) {
        isSystemInDarkMode = newConfig.isDarkModeEnabled
      }
    })
  }
}

private val Configuration.isDarkModeEnabled: Boolean
  get() {
    return if (BuildConfig.DEBUG) {
      uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    } else {
      true
    }
  }

fun appTheme(): AppTheme {
  return PressApp.component.theme()
}

fun View.themeAware(onThemeChange: (ThemePalette) -> Unit) {
  val stream = appTheme().listenRx()

  val attaches = attaches().mergeWith(Single.create {
    // Because RxBinding doesn't emit anything if the View is already attached.
    if (isAttachedToWindow) {
      it.onSuccess(Unit)
    }
  })

  attaches
    .switchMap { stream }
    .takeUntil(detaches())
    .mergeWith(stream.take(1))  // Don't wait till attach for the first emission.
    .distinctUntilChanged()
    .subscribe(onThemeChange)
}
