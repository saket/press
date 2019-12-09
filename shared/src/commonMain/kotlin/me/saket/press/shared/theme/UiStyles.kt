package me.saket.press.shared.theme

import me.saket.press.shared.theme.UiStyles.FontVariant.BOLD
import me.saket.press.shared.theme.UiStyles.FontVariant.REGULAR
import me.saket.press.shared.theme.UiStyles.Text
import me.saket.press.shared.theme.UiStyles.FontFamily.WORK_SANS

object HomeUiStyles {
  object NoteRow {
    val title = Text(
        font = WORK_SANS * BOLD,
        textSize = 16f,
        maxLines = 1
    )
    val body = Text(
        textSize = 15f,
        maxLines = 2
    )
  }
}

object EditorUiStyles {
  val editor = Text(
      textSize = 16f,
      lineSpacingMultiplier = 1.25f
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

    operator fun times(variant: FontVariant) = Font(this, variant)
  }

  enum class FontVariant(val weight: Int, val isItalic: Boolean) {
    REGULAR(weight = 400, isItalic = false),
    ITALIC(weight = 400, isItalic = true),
    BOLD(weight = 700, isItalic = false)
  }
}
