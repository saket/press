package press.sync

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorLong
import com.squareup.contour.ContourLayout
import me.saket.press.R
import me.saket.press.shared.theme.TextStyles
import me.saket.press.shared.theme.UiStyles
import me.saket.press.shared.theme.applyStyle
import press.theme.themeAware
import press.util.withOpacity
import press.widgets.attr

class RepoItemView(context: Context) : ContourLayout(context) {

  val nameView = TextView(context).apply {
    TextStyles.Secondary.applyStyle(this)
    applyLayout(
        x = matchParentX(marginLeft = 20.dip, marginRight = 20.dip),
        y = topTo { parent.top() + 8.ydip }
    )
  }

  val dividerView = View(context).apply {
    themeAware {
      background = ColorDrawable(
          ColorUtils.blendARGB(it.window.backgroundColor, Color.WHITE, 0.2f)
      )
    }
    applyLayout(
        x = leftTo { nameView.left() }.rightTo { nameView.right() },
        y = topTo { nameView.bottom() + 8.ydip }.heightOf { 1.ydip }
    )
  }

  init {
    contourHeightOf { dividerView.bottom() }
    background = attr(R.attr.selectableItemBackground).asDrawable()
  }
}
