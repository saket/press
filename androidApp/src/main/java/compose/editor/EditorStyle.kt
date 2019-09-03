package compose.editor

import compose.theme.AppStyle
import me.saket.compose.shared.theme.ThemePalette
import compose.theme.TextAppearance

class EditorStyle(
  palette: ThemePalette,
  val editor: TextAppearance = TextAppearance(color = palette.textColorSecondary)
): AppStyle(palette)