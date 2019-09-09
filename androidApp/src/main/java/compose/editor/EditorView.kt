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
import com.benasher44.uuid.uuid4
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import compose.theme.themeAware
import compose.theme.themed
import compose.widgets.fromOreo
import compose.widgets.hintRes
import compose.widgets.padding
import compose.widgets.textColor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.compose.R
import me.saket.compose.shared.contentModels
import me.saket.compose.shared.editor.EditorEvent
import me.saket.compose.shared.editor.EditorPresenter
import me.saket.compose.shared.editor.EditorUiModel
import me.saket.wysiwyg.Wysiwyg
import me.saket.wysiwyg.theme.DisplayUnits
import me.saket.wysiwyg.theme.WysiwygTheme
import me.saket.wysiwyg.widgets.addTextChangedListener

@SuppressLint("SetTextI18n")
class EditorView @AssistedInject constructor(
  @Assisted context: Context,
  presenterFactory: EditorPresenter.Factory
) : ContourLayout(context) {

  private val scrollView = themed(ScrollView(context)).apply {
    isFillViewport = true
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.bottomTo { parent.bottom() }
    )
  }

  private val editorEditText = themed(EditText(context)).apply {
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
    themeAware {
      textColor = it.textColorSecondary
    }
  }

  private val presenter = presenterFactory.create(
      noteUuid = uuid4()
  )

  init {
    scrollView.addView(editorEditText, MATCH_PARENT, WRAP_CONTENT)

    val wysiwyg = Wysiwyg(editorEditText, WysiwygTheme(DisplayUnits(context)))
    editorEditText.addTextChangedListener(wysiwyg.syntaxHighlighter())
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    Observable.empty<EditorEvent>()
        .contentModels(presenter)
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(::render)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    presenter.saveEditorContentOnExit(editorEditText.text)
  }

  private fun render(model: EditorUiModel) {

  }

  @AssistedInject.Factory
  interface Factory {
    fun withContext(context: Context): EditorView
  }
}
