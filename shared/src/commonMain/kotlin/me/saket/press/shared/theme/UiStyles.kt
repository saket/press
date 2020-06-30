package me.saket.press.shared.theme

import me.saket.press.shared.theme.TextStyles.Primary
import me.saket.press.shared.theme.TextStyles.Secondary
import me.saket.press.shared.theme.UiStyles.FontFamily.WORK_SANS
import me.saket.press.shared.theme.UiStyles.FontVariant.REGULAR

object HomeUiStyles {
  object NoteRow {
    val title = Primary.copy(
        maxLines = 1
    )
    val body = Secondary.copy(
        maxLines = 2,
        lineSpacingMultiplier = 1f
    )
  }
}

object EditorUiStyles {
  val editor = Primary.copy(
      font = WORK_SANS * REGULAR
  )
}

object UiStyles {

  data class Text(
    val font: Font = WORK_SANS * REGULAR,
    val textSize: Float,
    val lineSpacingMultiplier: Float = 1.0f,
    val maxLines: Int? = null
  )

  data class Font(
    val family: FontFamily,
    val variant: FontVariant
  )

  enum class FontFamily {
    WORK_SANS;

    /** e.g., WORK_SANS * REGULAR */
    operator fun times(variant: FontVariant) = Font(this, variant)
  }

  enum class FontVariant(val weight: Int, val isItalic: Boolean) {
    REGULAR(weight = 400, isItalic = false),
    ITALIC(weight = 400, isItalic = true),
    BOLD(weight = 700, isItalic = false)
  }
}
