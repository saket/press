package me.saket.wysiwyg

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt

actual data class WysiwygTheme(
  /** Used for resolving default colors and dimensions. */
  val context: Context,

  /** Color used for highlighting '**', '~~' and other syntax characters. */
  @ColorInt
  actual val syntaxColor: Int = color("#CCAEF9"),

  @ColorInt
  actual val linkUrlColor: Int = color("#9E9E9E"),

  @ColorInt
  actual val linkTextColor: Int = color("#1DE9B6")
)

@Suppress("SameParameterValue")
private fun color(hex: String) = Color.parseColor(hex)
