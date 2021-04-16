package me.saket.press.shared.theme

import me.saket.press.shared.theme.UiStyles.FontFamily.Regular
import me.saket.press.shared.theme.UiStyles.FontVariant.Normal
import me.saket.press.shared.theme.UiStyles.Typeface.WorkSans

object UiStyles {
  data class Text(
    val font: Font = Font(WorkSans, Regular, Normal),
    val textSize: Float,
    val lineSpacingMultiplier: Float = 1.0f,
    val maxLines: Int? = null
  )

  data class Font(
    val typeface: Typeface = WorkSans,
    val family: FontFamily,
    val variant: FontVariant
  )

  // Note to self:
  //  Roboto is a typeface.
  //  Roboto Mono is a font family.
  //  Roboto Mono Italic is a font.
  enum class Typeface(val displayName: String) {
    WorkSans("Work Sans"),
    System("System")
  }

  enum class FontFamily {
    Regular
  }

  enum class FontVariant(val weight: Int, val isItalic: Boolean) {
    Normal(weight = 400, isItalic = false),
    Italic(weight = 400, isItalic = true),
    Bold(weight = 700, isItalic = false)
  }
}
