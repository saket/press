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
  actual val blockQuoteVerticalRuleColor: Int = color("#CCAEF9"),

  @ColorInt
  actual val blockQuoteTextColor: Int = color("#BFFFFFFF"), // 75% white.

  /** Width of a block-quote's vertical line/stripe/rule. */
  @Px
  actual val blockQuoteVerticalRuleStrokeWidth: Int = dip(context, 4).toInt(),

  /** Gap before a block-quote. */
  @Px
  actual val blockQuoteIndentationMargin: Int = dip(context, 24).toInt(),

  /** Gap before a block of ordered/unordered list. */
  @Px
  actual val listBlockIndentationMargin: Int = dip(context, 24).toInt(),

  @ColorInt
  actual val linkUrlColor: Int = color("#7DFFFFFF"),  // 50% white.

  @ColorInt
  actual val linkTextColor: Int = color("#8DF0FF"),

  /** Thematic break a.k.a. horizontal rule. */
  @ColorInt
  actual val thematicBreakColor: Int = color("#62677C"),

  @Px
  actual val thematicBreakThickness: Float = dip(context, 4),

  @ColorInt
  actual val codeBackgroundColor: Int = color("#1F202A"),

  @Px
  actual val codeBlockMargin: Int = dip(context, 8).toInt(),

  @ColorInt
  actual val headingTextColor: Int = color("#50FA7B")
)

@Suppress("SameParameterValue")
private fun color(hex: String) = Color.parseColor(hex)

private fun dip(context: Context, @Px px: Int): Float =
  TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      px.toFloat(),
      context.resources.displayMetrics
  )
