@file:Suppress("unused")

package compose.editor

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
import android.view.Gravity.CENTER_VERTICAL
import android.view.Gravity.TOP
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import com.squareup.contour.ContourLayout
import compose.theme.autoApply
import compose.util.attr
import compose.util.fromOreo
import compose.util.heightOf
import compose.util.hintRes
import compose.util.padding
import compose.util.string
import compose.util.x
import io.reactivex.Observable
import me.saket.compose.R
import me.saket.wysiwyg.Wysiwyg
import me.saket.wysiwyg.theme.DisplayUnits
import me.saket.wysiwyg.theme.WysiwygTheme
import me.saket.wysiwyg.widgets.addTextChangedListener

@SuppressLint("SetTextI18n")
class EditorView(
  context: Context,
  style: Observable<EditorStyle>
) : ContourLayout(context) {

  private val toolbarView = TextView(context).apply {
    text = string(R.string.app_name)
    gravity = CENTER_VERTICAL
    style.map { it.toolbar.title }.autoApply(this)
    applyLayout(
        x = leftTo { parent.left() + 16.dip.x }.rightTo { parent.right() - 16.dip.x },
        y = topTo { parent.top() }.heightOf(attr(android.R.attr.actionBarSize))
    )
  }

  private val scrollView = ScrollView(context).apply {
    isFillViewport = true
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { toolbarView.bottom() }.bottomTo { parent.bottom() }
    )
  }

  private val editorEditText = EditText(context).apply {
    background = null
    breakStrategy = BREAK_STRATEGY_HIGH_QUALITY
    gravity = TOP
    hintRes = R.string.editor_hint
    inputType = TYPE_CLASS_TEXT or  // Multiline doesn't work without this.
        TYPE_TEXT_FLAG_CAP_SENTENCES or
        TYPE_TEXT_FLAG_MULTI_LINE or
        TYPE_TEXT_FLAG_NO_SUGGESTIONS
    padding = 16.dip
    fromOreo {
      importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
    }
    style.map { it.editor }.autoApply(this)
  }

  init {
    scrollView.addView(editorEditText, MATCH_PARENT, WRAP_CONTENT)

    editorEditText.setText(
        """
      |**Bold text**
      |*Italic text*
      |[Link](https://url.com)
      |~~Strikethrough~~
      |`Inline code`
      |
      |    Indented code block
      |    
      |```
      |Fenced code block
      |```
      |
      |> Block quote
      |
      |Ordered list block
      |1. Item A
      |2. Item B
      |
      |Unordered list block
      |- Item A
      |- Item B
      |
      |---
      |***
      |___
      |
      |# Heading 1
      |## Heading 2
      |### Heading 3
      |#### Heading 4
      |##### Heading 5
      |###### Heading 6
    """.trimMargin()
    )
    editorEditText.setSelection(editorEditText.text.length)

    val wysiwyg = Wysiwyg(editorEditText, WysiwygTheme(DisplayUnits(context)))
    editorEditText.addTextChangedListener(wysiwyg.syntaxHighlighter())
  }
}
