package me.saket.press.shared.theme

import me.saket.press.shared.theme.UiStyles.FontFamily.WORK_SANS
import me.saket.press.shared.theme.UiStyles.FontVariant.BOLD
import me.saket.press.shared.theme.UiStyles.FontVariant.REGULAR

object TextStyles {
  val mainTitle = UiStyles.Text(
    font = WORK_SANS * BOLD,
    textSize = 16f,
    lineSpacingMultiplier = 1.25f
  )

  val mainBody = UiStyles.Text(
    font = WORK_SANS * REGULAR,
    textSize = 16f,
    lineSpacingMultiplier = 1.25f
  )

  val smallTitle = UiStyles.Text(
    font = WORK_SANS * BOLD,
    textSize = 14f,
    lineSpacingMultiplier = 1.25f
  )

  val smallBody = UiStyles.Text(
    font = WORK_SANS * REGULAR,
    textSize = 14f,
    lineSpacingMultiplier = 1.25f
  )
}
