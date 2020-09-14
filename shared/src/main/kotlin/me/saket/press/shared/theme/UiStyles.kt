package me.saket.press.shared.theme

import android.content.Context
import android.graphics.Typeface
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.text.TextUtils.TruncateAt
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import me.saket.press.shared.BuildConfig
import me.saket.press.shared.R
import me.saket.press.shared.theme.UiStyles.FontFamily.WORK_SANS
import me.saket.press.shared.theme.UiStyles.FontVariant.BOLD
import me.saket.press.shared.theme.UiStyles.FontVariant.ITALIC
import me.saket.press.shared.theme.UiStyles.FontVariant.REGULAR

fun UiStyles.Text.applyStyle(view: TextView) {
  view.textSize = textSize
  view.typeface = font.asTypeface(view.context)
  view.setLineSpacing(0f, lineSpacingMultiplier)

  if (maxLines != null) {
    view.maxLines = maxLines
    view.ellipsize = TruncateAt.END
  }
}

fun UiStyles.Font.asTypeface(context: Context): Typeface {
  val fontFamily = ResourcesCompat.getFont(context, when (family) {
    WORK_SANS -> R.font.work_sans
  })

  return if (SDK_INT >= P) {
    val isItalic = variant.isItalic
    Typeface.create(fontFamily, variant.weight, isItalic)
  } else {
    if (BuildConfig.DEBUG && variant.weight > 400 && variant == ITALIC) {
      throw TODO("Find a way backward-compatible way to render composite styles (i.e., italic + bold)")
    }

    val styleInt = when (variant) {
      REGULAR -> Typeface.NORMAL
      ITALIC -> Typeface.ITALIC
      BOLD -> Typeface.BOLD
    }
    Typeface.create(fontFamily, styleInt)
  }
}
