package press.preferences.sync.setup

import android.content.Context
import android.view.Gravity.CENTER_HORIZONTAL
import com.squareup.contour.ContourLayout
import me.saket.press.shared.localization.strings
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import press.extensions.textColor
import press.theme.themeAware
import press.widgets.PressButton

class ErrorView(context: Context) : ContourLayout(context) {
  private val messageView = TextView(context, smallBody).apply {
    gravity = CENTER_HORIZONTAL
    text = context.strings().common.generic_error
    themeAware {
      textColor = it.textColorPrimary
    }
  }

  val retryButton = PressButton(context, smallBody).apply {
    text = context.strings().common.retry
  }

  init {
    messageView.applyLayout(
      x = matchParentX(),
      y = topTo { parent.top() }
    )

    retryButton.applyLayout(
      x = centerHorizontallyTo { parent.centerX() },
      y = topTo { messageView.bottom() + 16.ydip }
    )

    contourHeightOf { retryButton.bottom() }
  }
}
