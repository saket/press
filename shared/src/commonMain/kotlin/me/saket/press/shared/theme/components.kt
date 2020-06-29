package me.saket.press.shared.theme

import me.saket.press.shared.theme.UiStyles.FontFamily.WORK_SANS
import me.saket.press.shared.theme.UiStyles.FontVariant.BOLD
import me.saket.press.shared.theme.UiStyles.FontVariant.REGULAR

object TextStyles {
  val Primary = UiStyles.Text(
      font = WORK_SANS * BOLD,
      textSize = 16f,
      lineSpacingMultiplier = 1.25f
  )

  val Secondary = UiStyles.Text(
      font = WORK_SANS * REGULAR,
      textSize = 15f,
      lineSpacingMultiplier = 1.25f
  )
}
