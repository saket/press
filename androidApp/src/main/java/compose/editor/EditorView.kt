@file:Suppress("unused")

package compose.editor

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
import android.view.Gravity.TOP
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.ScrollView
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import compose.theme.autoApply
import compose.util.fromOreo
import compose.util.hintRes
import compose.util.padding
import io.reactivex.Observable
import me.saket.compose.R
import me.saket.wysiwyg.Wysiwyg
import me.saket.wysiwyg.theme.DisplayUnits
import me.saket.wysiwyg.theme.WysiwygTheme
import me.saket.wysiwyg.widgets.addTextChangedListener

@SuppressLint("SetTextI18n")
class EditorView @AssistedInject constructor(
  @Assisted context: Context,
  style: Observable<EditorStyle>
) : ContourLayout(context) {

  private val scrollView = ScrollView(context).apply {
    isFillViewport = true
    style.map { it.scrollView }.autoApply(this)
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.bottomTo { parent.bottom() }
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

    val wysiwyg = Wysiwyg(editorEditText, WysiwygTheme(DisplayUnits(context)))
    editorEditText.addTextChangedListener(wysiwyg.syntaxHighlighter())
  }

  @AssistedInject.Factory
  interface Factory {
    fun withContext(context: Context): EditorView
  }
}
