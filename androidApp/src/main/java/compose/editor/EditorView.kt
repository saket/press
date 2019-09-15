@file:Suppress("unused")

package compose.editor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Color.WHITE
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
import android.text.style.ForegroundColorSpan
import android.view.Gravity.TOP
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils.blendARGB
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.widget.textChanges
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import compose.theme.themeAware
import compose.theme.themed
import compose.util.exhaustive
import compose.widgets.Truss
import compose.widgets.fromOreo
import compose.widgets.padding
import compose.widgets.setTextAndCursor
import compose.widgets.textColor
import compose.widgets.textSizePx
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.compose.R.drawable
import me.saket.compose.shared.editor.EditorEvent
import me.saket.compose.shared.editor.EditorEvent.NoteTextChanged
import me.saket.compose.shared.editor.EditorOpenMode
import me.saket.compose.shared.editor.EditorPresenter
import me.saket.compose.shared.editor.EditorUiModel
import me.saket.compose.shared.editor.EditorUiUpdate
import me.saket.compose.shared.editor.EditorUiUpdate.CloseNote
import me.saket.compose.shared.editor.EditorUiUpdate.PopulateContent
import me.saket.compose.shared.localization.Strings
import me.saket.compose.shared.navigation.Navigator
import me.saket.compose.shared.navigation.ScreenKey.Back
import me.saket.compose.shared.uiModels
import me.saket.compose.shared.uiUpdates2
import me.saket.wysiwyg.Wysiwyg
import me.saket.wysiwyg.parser.node.HeadingLevel.H1
import me.saket.wysiwyg.theme.DisplayUnits
import me.saket.wysiwyg.theme.WysiwygTheme
import me.saket.wysiwyg.widgets.addTextChangedListener

@SuppressLint("SetTextI18n")
class EditorView @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted openMode: EditorOpenMode,
  @Assisted private val navigator: Navigator,
  presenterFactory: EditorPresenter.Factory,
  strings: Strings.Editor
) : ContourLayout(context) {

  private val toolbar = themed(Toolbar(context)).apply {
    navigationIcon = getDrawable(context, drawable.ic_close_24dp)
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }
    )
  }

  internal val scrollView = themed(ScrollView(context)).apply {
    isFillViewport = true
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  internal val editorEditText = themed(EditText(context)).apply {
    background = null
    breakStrategy = BREAK_STRATEGY_HIGH_QUALITY
    gravity = TOP
    inputType = TYPE_CLASS_TEXT or  // Multiline doesn't work without this.
        TYPE_TEXT_FLAG_CAP_SENTENCES or
        TYPE_TEXT_FLAG_MULTI_LINE or
        TYPE_TEXT_FLAG_NO_SUGGESTIONS
    padding = 16.dip
    CapitalizeOnHeadingStart.capitalize(this)
    fromOreo {
      importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
    }
    themeAware {
      textColor = it.textColorSecondary
    }
  }

  private val headingHintTextView = TextView(context).apply {
    textSizePx = editorEditText.textSize
    themeAware {
      textColor = blendARGB(it.window.backgroundColor, WHITE, 0.50f)
    }
    applyLayout(
        x = leftTo { scrollView.left() + editorEditText.paddingStart }
            .rightTo { scrollView.right() - editorEditText.paddingStart },
        y = topTo { scrollView.top() + editorEditText.paddingTop }
    )
  }

  private val presenter = presenterFactory.create(openMode)

  init {
    scrollView.addView(editorEditText, MATCH_PARENT, WRAP_CONTENT)
    bringChildToFront(scrollView)

    val wysiwyg = Wysiwyg(editorEditText, WysiwygTheme(DisplayUnits(context)))
    editorEditText.addTextChangedListener(wysiwyg.syntaxHighlighter())

    toolbar.setNavigationOnClickListener {
      // TODO: detect if the keyboard is up and delay going back by
      //  a bit so that the IRV behind is resized before this View
      //  start collapsing.
      navigator.goTo(Back)
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val noteTextChanges: Observable<EditorEvent> = editorEditText
        .textChanges()
        .map { NoteTextChanged(it.toString()) }

    Observable.mergeArray(noteTextChanges)
        .uiModels(presenter)
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(::render)

    presenter.uiUpdates2()
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(::render)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    presenter.saveEditorContentOnExit(editorEditText.text)
  }

  private fun render(model: EditorUiModel) {
    headingHintTextView.text = Truss()
        .pushSpan(EditorHeadingHintSpan(H1))
        .pushSpan(ForegroundColorSpan(Color.TRANSPARENT))
        // Using a space character doesn't consume the same width
        // as '#'. Probably because the font isn't monospaced.
        .append("# ")
        .popSpan()
        .append(model.hintText ?: "")
        .popSpan()
        .build()
    headingHintTextView.visibility = if (model.hintText.isNullOrBlank()) GONE else VISIBLE
  }

  private fun render(uiUpdate: EditorUiUpdate) {
    when (uiUpdate) {
      is PopulateContent -> {
        editorEditText.setTextAndCursor(uiUpdate.content)
      }
      is CloseNote -> {
        navigator.goTo(Back)
      }
    }.exhaustive
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(
      context: Context,
      openMode: EditorOpenMode,
      navigator: Navigator
    ): EditorView
  }
}
