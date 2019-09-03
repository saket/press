package compose.editor

import compose.theme.AppStyle
import compose.theme.AppTheme
import compose.theme.TextAppearance

class EditorStyle(
  theme: AppTheme,
  val editor: TextAppearance = TextAppearance(color = theme.textColorSecondary)
): AppStyle(theme)