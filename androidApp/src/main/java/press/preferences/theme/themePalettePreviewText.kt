package press.preferences.theme

import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans
import me.saket.press.shared.theme.palettes.ThemePalette
import me.saket.wysiwyg.parser.node.HeadingLevel.H3
import me.saket.wysiwyg.spans.HeadingSpan
import me.saket.wysiwyg.spans.Recycler
import me.saket.wysiwyg.spans.StyleSpan
import me.saket.wysiwyg.spans.StyleSpan.Style.ITALIC

/**
 * Parsing markdown using wysiwyg is too expensive to do on the main thread.
 * Moving it off the main thread isn't too clean either so this View builds
 * markdown styling manually.
 */
fun ThemePalette.createPreviewMarkdownText(title: String): CharSequence {
  val fakeRecycler: Recycler = {}

  return buildSpannedString {
    inSpans(HeadingSpan(fakeRecycler).apply { level = H3 }) {
      color(markdown.syntaxColor) {
        append("###")
      }
      color(textColorHeading) {
        append(" $title\n")
      }
    }

    append("To live is to ")
    inSpans(StyleSpan(fakeRecycler).apply { style = ITALIC }) {
      color(markdown.syntaxColor) {
        append("*")
      }
      append("risk it all")
      color(markdown.syntaxColor) {
        append("*")
      }
    }

    append(", otherwise you're just an ")
    color(markdown.linkTextColor) {
      append("[inert chunk]")
    }
    color(markdown.linkUrlColor) {
      append("(...)")
    }

    append(" of randomly assembled molecules drifting wherever the universe blows you.")
  }
}
