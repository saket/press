package me.saket.press.shared.theme

import android.graphics.Typeface
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.text.TextUtils.TruncateAt
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import me.saket.press.shared.R
import me.saket.press.shared.theme.UiStyles.FontVariant.BOLD
import me.saket.press.shared.theme.UiStyles.FontVariant.ITALIC
import me.saket.press.shared.theme.UiStyles.FontVariant.REGULAR
import me.saket.press.shared.theme.UiStyles.Typeface.WORK_SANS

fun UiStyles.Text.applyStyle(view: TextView) {
  view.textSize = textSize
  view.typeface = readFont(view)
  view.setLineSpacing(0f, lineSpacingMultiplier)

  if (maxLines != null) {
    view.maxLines = maxLines
    view.ellipsize = TruncateAt.END
  }
}

private fun UiStyles.Text.readFont(view: TextView): Typeface {
  val typefaceRes = when (font.typeface) {
    WORK_SANS -> R.font.work_sans
  }
  val typeface = ResourcesCompat.getFont(view.context, typefaceRes)

  return if (SDK_INT >= P) {
    val isItalic = font.variant.isItalic
    Typeface.create(typeface, font.variant.weight, isItalic)
  } else {
    val styleInt = when (font.variant) {
      REGULAR -> Typeface.NORMAL
      ITALIC -> Typeface.ITALIC
      BOLD -> Typeface.BOLD
    }
    Typeface.create(typeface, styleInt)
  }
}
