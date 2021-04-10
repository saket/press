package me.saket.press.shared.theme

import me.saket.press.shared.theme.UiStyles.Font
import me.saket.press.shared.theme.UiStyles.FontFamily.Regular
import me.saket.press.shared.theme.UiStyles.FontVariant.Bold
import me.saket.press.shared.theme.UiStyles.FontVariant.Normal

object TextStyles {
  val mainTitle = UiStyles.Text(
    font = Font(Regular, Bold),
    textSize = 16f,
    lineSpacingMultiplier = 1.25f
  )

  val mainBody = UiStyles.Text(
    font = Font(Regular, Normal),
    textSize = 16f,
    lineSpacingMultiplier = 1.25f
  )

  val smallTitle = UiStyles.Text(
    font = Font(Regular, Bold),
    textSize = 14f,
    lineSpacingMultiplier = 1.25f
  )

  val smallBody = UiStyles.Text(
    font = Font(Regular, Normal),
    textSize = 14f,
    lineSpacingMultiplier = 1.25f
  )

  // todo: find a better name?
  val tinyBody = UiStyles.Text(
    font = Font(Regular, Normal),
    textSize = 12f,
    lineSpacingMultiplier = 1.25f
  )
}
