@file:Suppress("unused")

package compose.editor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
import androidx.core.graphics.ColorUtils
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.widget.textChanges
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import compose.theme.themeAware
import compose.theme.themed
import compose.util.exhaustive
import compose.widgets.AfterTextChange
import compose.widgets.Truss
import compose.widgets.fromOreo
import compose.widgets.padding
import compose.widgets.setTextAndCursor
import compose.widgets.textColor
import compose.widgets.textSizePx
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.compose.R.drawable
import me.saket.compose.shared.contentModels
import me.saket.compose.shared.editor.EditorEvent
import me.saket.compose.shared.editor.EditorEvent.NoteTextChanged
import me.saket.compose.shared.editor.EditorOpenMode
import me.saket.compose.shared.editor.EditorPresenter
import me.saket.compose.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.compose.shared.editor.EditorUiModel
import me.saket.compose.shared.editor.EditorUiModel.TransientUpdate.CloseNote
import me.saket.compose.shared.editor.EditorUiModel.TransientUpdate.PopulateContent
import me.saket.compose.shared.localization.Strings
import me.saket.compose.shared.navigation.Navigator
import me.saket.compose.shared.navigation.ScreenKey.Back
import me.saket.compose.shared.transientUpdates
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

  private val scrollView = themed(ScrollView(context)).apply {
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
    fromOreo {
      importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
    }
    themeAware {
      textColor = it.textColorSecondary
    }
  }

  private val headingHintTextView = TextView(context).apply {
    textSizePx = editorEditText.textSize
    text = Truss()
        .pushSpan(EditorHeadingHintSpan(H1))
        .pushSpan(ForegroundColorSpan(Color.TRANSPARENT))
        // Using a space character doesn't consume the same width
        // as '#'. Probably because the font isn't monospaced.
        .append("# ")
        .popSpan()
        .append(strings.newNoteHints.shuffled().first())
        .popSpan()
        .build()
    themeAware {
      textColor = ColorUtils.blendARGB(it.windowTheme.backgroundColor, Color.WHITE, 0.50f)
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
    editorEditText.setTextAndCursor(NEW_NOTE_PLACEHOLDER)

    toolbar.setNavigationOnClickListener {
      navigator.goTo(Back)
    }

    editorEditText.addTextChangedListener(AfterTextChange {
      headingHintTextView.visibility = GONE
    })
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    editorEditText.requestFocus()

    val noteTextChanges: Observable<EditorEvent> = editorEditText
        .textChanges()
        .map(::NoteTextChanged)

    val uiModels = Observable.mergeArray(noteTextChanges)
        .contentModels(presenter)
        .takeUntil(detaches())
        .observeOn(mainThread())

    uiModels.subscribe(::render)

    uiModels.transientUpdates()
        .observeOn(mainThread())
        .subscribe(::render)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    presenter.saveEditorContentOnExit(editorEditText.text)
  }

  private fun render(model: EditorUiModel) {}

  private fun render(uiUpdate: EditorUiModel.TransientUpdate) {
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
