@file:Suppress("unused")

package compose.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.Typeface.NORMAL
import android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
import android.view.Gravity.CENTER_VERTICAL
import android.view.Gravity.TOP
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import com.squareup.contour.ContourLayout
import me.saket.compose.R
import compose.theme.AppTheme
import compose.util.attr
import compose.util.fromOreo
import compose.util.heightOf
import compose.util.hintRes
import compose.util.padding
import compose.util.string
import compose.util.textAppearance
import compose.util.textColor
import compose.util.x

@SuppressLint("ViewConstructor")
class EditorView(
  context: Context,
  theme: AppTheme
) : ContourLayout(context) {

  private val toolbar = TextView(context).apply {
    text = string(R.string.app_name)
    typeface = Typeface.create("monospace", NORMAL)
    textAppearance = R.style.TextAppearance_AppCompat_Title
    textColor = theme.accentColor
    gravity = CENTER_VERTICAL
    applyLayout(
        x = leftTo { parent.left() + 16.dip.x }.rightTo { parent.right() - 16.dip.x },
        y = topTo { parent.top() }.heightOf(attr(android.R.attr.actionBarSize))
    )
  }

  private val scrollView = ScrollView(context).apply {
    isFillViewport = true
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  private val editor = EditText(context).apply {
    background = null
    breakStrategy = BREAK_STRATEGY_HIGH_QUALITY
    gravity = TOP
    hintRes = R.string.editor_hint
    inputType = TYPE_TEXT_FLAG_CAP_SENTENCES or TYPE_TEXT_FLAG_MULTI_LINE
    padding = 16.dip
    fromOreo {
      importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
    }
  }

  init {
    scrollView.addView(editor, MATCH_PARENT, WRAP_CONTENT)
  }
}
