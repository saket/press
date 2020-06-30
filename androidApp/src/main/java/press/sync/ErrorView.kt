package press.sync

import android.content.Context
import android.view.Gravity.CENTER_HORIZONTAL
import android.widget.Button
import android.widget.TextView
import com.squareup.contour.ContourLayout
import me.saket.press.shared.localization.strings
import press.theme.themed

class ErrorView(context: Context) : ContourLayout(context) {

  private val messageView = themed(TextView(context)).apply {
    gravity = CENTER_HORIZONTAL
    text = context.strings().common.genericError
    applyLayout(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
  }

  val retryButton = themed(Button(context)).apply {
    text = context.strings().common.retry
    applyLayout(
        x = centerHorizontallyTo { parent.centerX() },
        y = topTo { messageView.bottom() + 8.ydip }
    )
  }

  init {
    contourHeightOf { retryButton.bottom() }
  }
}
