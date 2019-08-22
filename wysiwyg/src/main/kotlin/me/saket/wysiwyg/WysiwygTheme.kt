package me.saket.wysiwyg

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt

actual data class WysiwygTheme(
  /** Used for resolving default colors and dimensions. */
  val context: Context,

  /** Color used for highlighting '**', '~~' and other syntax characters. */
  @ColorInt
  actual val syntaxColor: Int = color("#CCAEF9")
)

@Suppress("SameParameterValue")
private fun color(hex: String) = Color.parseColor(hex)
