package me.saket.press.shared.home

import me.saket.press.shared.theme.TextStyles.mainTitle
import me.saket.press.shared.theme.TextStyles.smallBody

object HomeUiStyles {
  val noteTitle = mainTitle.copy(maxLines = 1)
  val noteBody = smallBody.copy(maxLines = 2, lineSpacingMultiplier = 1f)
}
