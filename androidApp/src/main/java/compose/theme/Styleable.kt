package compose.theme

import android.view.View
import com.jakewharton.rxbinding3.view.attaches
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable

interface Styleable<T : View> {
  fun style(view: T)
}

fun <T : View> Observable<out Styleable<T>>.autoApply(view: T) {
  view.attaches()
      .switchMap { this }
      .takeUntil(view.detaches())
      .subscribe { it.style(view) }
}