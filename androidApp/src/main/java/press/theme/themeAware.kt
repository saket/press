package press.theme

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.attaches
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import me.saket.press.shared.theme.ThemePalette
import press.PressApp
import press.extensions.onDestroys

fun themePalette(): Observable<ThemePalette> = PressApp.component.themePalette()

fun View.themeAware(onThemeChange: (ThemePalette) -> Unit) {
  val stream = themePalette()
  attaches()
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

