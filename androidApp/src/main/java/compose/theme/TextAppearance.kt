package compose.theme

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.StyleRes
import com.jakewharton.rxbinding3.view.attaches
import com.jakewharton.rxbinding3.view.detaches
import compose.util.textColor
import io.reactivex.Observable

data class TextAppearance(
  @StyleRes val parentRes: Int? = null,
  val textSizeSp: Float = 15f,
  val letterSpacing: Float = 0f,
  val textColor: Int,
  val typeface: Typeface = Typeface.DEFAULT
) {

  fun style(view: TextView) {
    if (parentRes != null) {
      view.setTextAppearance(parentRes)
    }

    view.textSize = textSizeSp
    view.letterSpacing = letterSpacing
    view.textColor = textColor
  }
}

@SuppressLint("CheckResult")
fun Observable<TextAppearance>.autoApply(view: TextView) {
  view.attaches()
      .switchMap { this }
      .takeUntil(view.detaches())
      .subscribe { it.style(view) }
}