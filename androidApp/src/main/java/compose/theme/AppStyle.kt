package compose.theme

import android.graphics.drawable.ColorDrawable
import compose.util.titleView
import compose.widgets.DisplayUnit.Dp
import compose.widgets.dp
import me.saket.compose.R
import androidx.appcompat.widget.Toolbar as ToolbarView

abstract class AppStyle(
  palette: ThemePalette,
  val toolbar: ToolbarStyle = ToolbarStyle(palette)
)

class ToolbarStyle(
  palette: ThemePalette,
  val title: TextAppearance = TextAppearance(
      parentRes = R.style.TextAppearance_AppCompat_Title,
      color = palette.accentColor
  ),
  private val backgroundColor: Int = palette.primaryColor,
  private val elevation: Dp = 4.dp
): Styleable<ToolbarView> {

  override fun style(view: ToolbarView) {
    title.style(view.titleView)
    view.background = ColorDrawable(backgroundColor)
    view.elevation = elevation.px(view.context)
  }
}

