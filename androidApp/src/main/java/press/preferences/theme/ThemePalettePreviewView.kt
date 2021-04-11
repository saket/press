package press.preferences.theme

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.view.updatePaddingRelative
import com.squareup.contour.ContourLayout
import me.saket.press.shared.theme.DisplayUnits
import me.saket.press.shared.theme.TextStyles
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.theme.ThemePalette
import me.saket.press.shared.theme.palettes.wysiwygStyle
import me.saket.wysiwyg.Wysiwyg
import me.saket.wysiwyg.Wysiwyg.Companion
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
    val markdown = """
        ### ${palette.name}
        To live is to *risk it all*, otherwise you're just an [inert chunk](...) of randomly assembled \
        molecules drifting wherever the universe blows you.
        """.trimIndent().replace("\\\n", "")

    previewTextView.let {
      it.textColor = palette.textColorPrimary
      it.text = Wysiwyg.highlightImmediately(markdown, palette.wysiwygStyle(DisplayUnits(context)))
    }

    background = Drawables.roundedRect(
        palette.window.elevatedBackgroundColor, cornerRadius = dp(4f)
    )
      .withRipple(rippleColor = palette.accentColor)
  }
}
