package me.saket.press.shared.theme

import me.saket.press.shared.theme.UiStyles.FontWeight.BOLD
import me.saket.press.shared.theme.UiStyles.FontWeight.REGULAR
import me.saket.press.shared.theme.UiStyles.Text
import me.saket.press.shared.theme.UiStyles.Typeface.WORK_SANS

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
    val typeface: Typeface,
    val weight: FontWeight
  )

  enum class Typeface {
    WORK_SANS;

    operator fun times(weight: FontWeight) = Font(this, weight)
  }

  enum class FontWeight {
    REGULAR,
    BOLD
  }
}
