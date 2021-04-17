package me.saket.press.shared.theme

import android.content.Context
import android.graphics.Typeface
import android.os.Build.VERSION.SDK_INT
import android.text.TextUtils.TruncateAt.END
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import me.saket.press.shared.R
import me.saket.press.shared.theme.UiStyles.FontFamily.Regular
import me.saket.press.shared.theme.UiStyles.FontVariant.Bold
import me.saket.press.shared.theme.UiStyles.FontVariant.Italic
import me.saket.press.shared.theme.UiStyles.FontVariant.Normal
import me.saket.press.shared.theme.UiStyles.Typeface.System
import me.saket.press.shared.theme.UiStyles.Typeface.WorkSans

fun TextView.applyStyle(style: UiStyles.Text) {
  textSize = style.textSize
  typeface = style.font.asTypeface(context)
  setLineSpacing(0f, style.lineSpacingMultiplier)

  if (style.maxLines != null) {
    maxLines = style.maxLines
    ellipsize = END
  }
}

fun UiStyles.Typeface.asAndroidTypeface(context: Context): Typeface {
  return UiStyles.Font(
    typeface = this,
    family = Regular,
    variant = Normal
  ).asTypeface(context)
}

fun UiStyles.Font.asTypeface(context: Context): Typeface {
  val fontFamily: Typeface = when (typeface) {
    WorkSans -> {
      val fontResId = when (family) {
        Regular -> R.font.work_sans
      }
      ResourcesCompat.getFont(context, fontResId)!!
    }
    System -> Typeface.create("sans-serif-thin", Typeface.NORMAL)
  }

  return if (SDK_INT >= 28) {
    val isItalic = variant.isItalic
    Typeface.create(fontFamily, variant.weight, isItalic)
  } else {
    if (variant.weight > 400 && variant == Italic) {
      error("Find a way backward-compatible way to render composite styles (i.e., italic + bold)")
    }

    val styleInt = when (variant) {
      Normal -> Typeface.NORMAL
      Italic -> Typeface.ITALIC
      Bold -> Typeface.BOLD
    }
    Typeface.create(fontFamily, styleInt)
  }
}

@Suppress("FunctionName")
fun TextView(context: Context, style: UiStyles.Text): TextView {
  return TextView(context).apply { applyStyle(style) }
}

@Suppress("FunctionName")
fun EditText(context: Context, style: UiStyles.Text): EditText {
  return EditText(context).apply { applyStyle(style) }
}
