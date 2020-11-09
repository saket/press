package press.editor

import android.content.Context
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
import android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
import android.util.AttributeSet
import android.view.Gravity.TOP
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN
import android.widget.EditText
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.updatePaddingRelative
import androidx.core.widget.NestedScrollView
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.inject.inflation.InflationInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import kotlinx.android.parcel.Parcelize
import me.saket.press.R
import me.saket.press.shared.editor.AutoCorrectEnabled
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorOpenMode
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.EditorPresenter.Args
import me.saket.press.shared.editor.EditorUiEffect
import me.saket.press.shared.editor.EditorUiEffect.BlockedDueToSyncConflict
import me.saket.press.shared.editor.EditorUiEffect.UpdateNoteText
import me.saket.press.shared.editor.EditorUiModel
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.theme.DisplayUnits
import me.saket.press.shared.theme.TextStyles.mainBody
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.theme.from
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import me.saket.wysiwyg.Wysiwyg
import me.saket.wysiwyg.formatting.TextSelection
import me.saket.wysiwyg.parser.node.HeadingLevel.H1
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.addTextChangedListener
import press.extensions.doOnTextChange
import press.extensions.fromOreo
import press.extensions.textColor
import press.extensions.textSizePx
import press.navigation.ScreenKey
import press.navigation.navigator
import press.theme.themeAware
import press.theme.themePalette
import press.widgets.PressToolbar

@Parcelize
object EditorScreenKey : ScreenKey(EditorView::class)

class EditorView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  @Assisted openMode: EditorOpenMode,
  presenterFactory: EditorPresenter.Factory,
  autoCorrectEnabled: Setting<AutoCorrectEnabled>
) : ContourLayout(context) {

  private val toolbar = PressToolbar(context).apply {
    themeAware {
      setBackgroundColor(it.window.editorBackgroundColor)
    }
    applyLayout(
      x = leftTo { parent.left() }.rightTo { parent.right() },
      y = topTo { parent.top() }
    )
  }

  internal val scrollView = NestedScrollView(context).apply {
    id = R.id.editor_scrollable_container
    isFillViewport = true
    applyLayout(
      x = leftTo { parent.left() }.rightTo { parent.right() },
      y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  internal val editorEditText = PlainTextPasteEditText(context).apply {
    applyStyle(mainBody)
    id = R.id.editor_textfield
    background = null
    breakStrategy = BREAK_STRATEGY_HIGH_QUALITY
    gravity = TOP
    inputType = TYPE_CLASS_TEXT or  // Multiline doesn't work without this.
      TYPE_TEXT_FLAG_CAP_SENTENCES or
      TYPE_TEXT_FLAG_MULTI_LINE or
      TYPE_TEXT_FLAG_NO_SUGGESTIONS
    if (autoCorrectEnabled.get()!!.enabled) {
      inputType = inputType or TYPE_TEXT_FLAG_AUTO_CORRECT
    }
    imeOptions = IME_FLAG_NO_FULLSCREEN
    movementMethod = EditorLinkMovementMethod(scrollView)
    filters += FormatMarkdownOnEnterPress(this)
    CapitalizeOnHeadingStart.capitalize(this)
    updatePaddingRelative(start = 20.dip, end = 20.dip, bottom = 20.dip)
    fromOreo {
      importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
    }
    themeAware {
      textColor = it.textColorPrimary
    }
  }

  private val headingHintTextView = TextView(context, mainBody).apply {
    textSizePx = editorEditText.textSize
    themeAware {
      textColor = it.textColorHint
    }
    applyLayout(
      x = leftTo { scrollView.left() + editorEditText.paddingStart }
        .rightTo { scrollView.right() - editorEditText.paddingStart },
      y = topTo { scrollView.top() + editorEditText.paddingTop }
    )
  }

  private val presenter = presenterFactory.create(
    Args(
      openMode = openMode,
      deleteBlankNewNoteOnExit = true,
      navigator = navigator()
    )
  )

  init {
    scrollView.addView(editorEditText, MATCH_PARENT, WRAP_CONTENT)
    bringChildToFront(scrollView)

    themeAware { palette ->
      setBackgroundColor(palette.window.editorBackgroundColor)
    }

    // TODO: add support for changing WysiwygStyle.
    themePalette()
      .take(1)
      .takeUntil(detaches())
      .subscribe { palette ->
        val wysiwygStyle = WysiwygStyle.from(palette.markdown, DisplayUnits(context))
        val wysiwyg = Wysiwyg(editorEditText, wysiwygStyle)
        editorEditText.addTextChangedListener(wysiwyg.syntaxHighlighter())
      }

    toolbar.setNavigationOnClickListener {
      navigator().goBack()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    editorEditText.doOnTextChange {
      presenter.dispatch(NoteTextChanged(it.toString()))
    }

    presenter.uiUpdates()
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe(models = ::render, effects = ::render)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    presenter.saveEditorContentOnClose(editorEditText.text.toString())
  }

  private fun render(model: EditorUiModel) {
    if (model.hintText == null) {
      headingHintTextView.visibility = GONE
    } else {
      headingHintTextView.visibility = VISIBLE
      headingHintTextView.text = buildSpannedString {
        inSpans(EditorHeadingHintSpan(H1)) {
          append(model.hintText!!)
        }
      }
    }
  }

  private fun render(uiUpdate: EditorUiEffect) {
    return when (uiUpdate) {
      is UpdateNoteText -> editorEditText.setText(uiUpdate.newText, uiUpdate.newSelection)
      is BlockedDueToSyncConflict -> EditingBlockedDueToConflictDialog.show(context, onDismiss = navigator()::goBack)
    }
  }

  private fun EditText.setText(newText: CharSequence, newSelection: TextSelection?) {
    setText(newText)
    newSelection?.let {
      setSelection(it.start, it.end)
    }
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(
      context: Context,
      openMode: EditorOpenMode
    ): EditorView
  }
}
