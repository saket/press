package press.preferences.editor

import android.content.Context
import com.squareup.contour.ContourLayout
import me.saket.press.shared.theme.DisplayUnits
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.theme.palettes.wysiwygStyle
import me.saket.wysiwyg.Wysiwyg
import press.extensions.textColor
import press.extensions.updatePadding
import press.theme.themeAware
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

    val markdown = """
        ### Preview
        To live is to *risk it all*, otherwise you're just an [inert chunk](...) of randomly assembled \
        molecules drifting wherever the universe blows you.
        """.trimIndent().replace("\\\n", "")

    themeAware { palette ->
      setBackgroundColor(palette.window.elevatedBackgroundColor)

      previewTextView.let {
        it.textColor = palette.textColorPrimary
        it.text = Wysiwyg.highlightImmediately(markdown, palette.wysiwygStyle(DisplayUnits(context)))
      }
    }
  }
}
