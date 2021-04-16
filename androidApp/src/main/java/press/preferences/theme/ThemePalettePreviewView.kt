package press.preferences.theme

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.view.updatePaddingRelative
import com.squareup.contour.ContourLayout
import me.saket.press.shared.theme.TextStyles
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.theme.ThemePalette
import press.extensions.textColor
import press.widgets.Drawables
import press.widgets.dp
import press.widgets.withRipple

class ThemePalettePreviewView(context: Context) : ContourLayout(context) {
  private val previewTextView = TextView(context, TextStyles.tinyBody).apply {
    maxLines = 4
    updatePaddingRelative(start = dp(24), top = dp(20), end = -dp(20))
  }
  lateinit var palette: ThemePalette

  init {
    elevation = 2f.dip

    previewTextView.layoutBy(
      // The text extends beyond this View's right bounds.
      // This design was copied from Bear. Looks pretty cool.
      x = leftTo { parent.left() }.rightTo { parent.right() + 50.xdip },
      y = topTo { parent.top() }
    )
    contourWidthOf { 200.xdip }
    contourHeightOf { previewTextView.bottom() }
  }

  @SuppressLint("SetTextI18n")
  fun render(palette: ThemePalette) {
    this.palette = palette

    previewTextView.textColor = palette.textColorPrimary
    previewTextView.text = palette.createPreviewMarkdownText(title = palette.name)

    background = Drawables.roundedRect(palette.window.elevatedBackgroundColor, cornerRadius = dp(4f))
      .withRipple(rippleColor = palette.accentColor)
  }
}
