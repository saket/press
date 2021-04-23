package press.preferences.editor

import android.content.Context
import com.squareup.contour.ContourLayout
import me.saket.press.shared.localization.strings
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import press.extensions.textColor
import press.extensions.updatePadding
import press.preferences.theme.createPreviewMarkdownText
import press.theme.themePalette
import press.widgets.dp

class EditorPreviewView(context: Context) : ContourLayout(context) {
  private val previewTextView = TextView(context, smallBody).apply {
    updatePadding(horizontal = dp(20), vertical = dp(40))
  }

  init {
    previewTextView.layoutBy(
      // The text extends beyond this View's right bounds.
      // This design was copied from Bear. Looks pretty cool.
      x = leftTo { parent.left() }.rightTo { parent.right() },
      y = topTo { parent.top() }
    )
    contourHeightOf { previewTextView.bottom() }
    setBackgroundColor(themePalette().window.elevatedBackgroundColor)

    previewTextView.let {
      it.textColor = themePalette().textColorPrimary
      it.text = themePalette().createPreviewMarkdownText(title = context.strings().prefs.editor_preview_title)
    }
  }
}
