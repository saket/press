package me.saket.wysiwyg

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.Px

actual data class WysiwygTheme(
  /** Used for resolving default colors and dimensions. */
  val context: Context,

  /** Color used for highlighting '**', '~~' and other syntax characters. */
  @ColorInt
  actual val syntaxColor: Int = color("#CCAEF9"),

  @ColorInt
  actual val linkUrlColor: Int = color("#7DFFFFFF"),

  @ColorInt
  actual val linkTextColor: Int = color("#8DF0FF"),

  @ColorInt
  actual val codeBackgroundColor: Int = color("#1F202A"),

  @Px
  actual val codeBlockMargin: Int = dip(context, 8).toInt()
)

@Suppress("SameParameterValue")
private fun color(hex: String) = Color.parseColor(hex)

private fun dip(context: Context, @Px px: Int): Float =
  TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      px.toFloat(),
      context.resources.displayMetrics
  )
