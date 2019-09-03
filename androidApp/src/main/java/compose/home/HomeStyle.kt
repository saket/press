package compose.home

import compose.theme.AppStyle
import compose.theme.AppTheme
import compose.theme.TextAppearance

class HomeStyle(
  theme: AppTheme,
  val noteRow: NoteRow = NoteRow(theme)
) : AppStyle(theme) {

  class NoteRow(
    theme: AppTheme,
    val title: TextAppearance = TextAppearance(color = theme.headingColor),
    val body: TextAppearance = TextAppearance(color = theme.textColorSecondary)
  )
}