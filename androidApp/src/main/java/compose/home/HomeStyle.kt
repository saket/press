package compose.home

import compose.theme.AppStyle
import compose.theme.TextAppearance
import compose.theme.ThemePalette

class HomeStyle(
  palette: ThemePalette,
  val noteRow: NoteRow = NoteRow(palette)
) : AppStyle(palette) {

  class NoteRow(
    palette: ThemePalette,
    val title: TextAppearance = TextAppearance(color = palette.headingColor),
    val body: TextAppearance = TextAppearance(color = palette.textColorSecondary)
  )
}