package me.saket.press.shared.theme

import android.text.TextUtils.TruncateAt
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import me.saket.press.shared.theme.UiStyles.FontWeight.BOLD
import me.saket.press.shared.theme.UiStyles.FontWeight.REGULAR
import me.saket.press.shared.theme.UiStyles.Typeface.WORK_SANS
import me.saket.press.shared.R

fun UiStyles.Text.applyStyle(view: TextView) {
  view.textSize = textSize
  view.setLineSpacing(0f, lineSpacingMultiplier)

  val fontRes = when (font.typeface) {
    WORK_SANS -> when (font.weight) {
      REGULAR -> R.font.work_sans_regular
      BOLD -> R.font.work_sans_bold
    }
  }
  view.typeface = ResourcesCompat.getFont(view.context, fontRes)

  if (maxLines != null) {
    view.maxLines = maxLines
    view.ellipsize = TruncateAt.END
  }
}
