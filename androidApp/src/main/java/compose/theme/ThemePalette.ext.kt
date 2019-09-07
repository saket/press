package compose.theme

import android.view.View
import com.jakewharton.rxbinding3.view.attaches
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import me.saket.compose.shared.theme.ThemePalette

inline fun Observable<ThemePalette>.listen(
  view: View,
  crossinline listener: (ThemePalette) -> Unit
) {
  view.attaches()
      .switchMap { this }
      .takeUntil(view.detaches())
      .subscribe { listener(it) }
}
