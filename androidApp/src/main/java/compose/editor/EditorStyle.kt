package compose.editor

import android.graphics.Typeface
import compose.theme.AppTheme
import compose.theme.TextAppearance
import me.saket.compose.R

class EditorStyle(
  theme: AppTheme,
  val toolbar: TextAppearance = TextAppearance(
      parentRes = R.style.TextAppearance_AppCompat_Title,
      typeface = Typeface.create("monospace", Typeface.NORMAL),
      textColor = theme.palette.accentColor
  ),
  val editor: TextAppearance = TextAppearance(
      textColor = theme.palette.textColorSecondary
  )
)