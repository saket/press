package press.theme

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.attaches
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.Single
import me.saket.press.shared.listenRx
import me.saket.press.shared.theme.AppTheme
import me.saket.press.shared.theme.ThemePalette
import press.PressApp
import press.extensions.onDestroys

fun appTheme(): AppTheme = PressApp.component.theme()

fun themePalette(): Observable<ThemePalette> =
  appTheme().listenRx()

fun View.themeAware(onThemeChange: (ThemePalette) -> Unit) {
  val stream = themePalette()

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
    .subscribe { onThemeChange(it) }
}

fun AppCompatActivity.themeAware(onThemeChange: (ThemePalette) -> Unit) {
  themePalette()
    .takeUntil(onDestroys())
    .subscribe { onThemeChange(it) }
}
