@file:Suppress("unused")

package press.editor

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
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils.blendARGB
import androidx.core.view.updatePaddingRelative
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.widget.textChanges
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.R.drawable
import me.saket.press.shared.editor.EditorEvent
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorOpenMode
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.EditorUiModel
import me.saket.press.shared.editor.EditorUiUpdate
import me.saket.press.shared.editor.EditorUiUpdate.CloseNote
import me.saket.press.shared.editor.EditorUiUpdate.PopulateContent
import me.saket.press.shared.navigation.Navigator
import me.saket.press.shared.navigation.ScreenKey.Back
import me.saket.press.shared.theme.EditorUiStyles
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.uiModels
import me.saket.press.shared.uiUpdates2
import me.saket.wysiwyg.Wysiwyg
import me.saket.wysiwyg.parser.node.HeadingLevel.H1
import me.saket.wysiwyg.theme.DisplayUnits
import me.saket.wysiwyg.theme.WysiwygTheme
import me.saket.wysiwyg.widgets.addTextChangedListener
import press.theme.themeAware
import press.theme.themed
import press.util.exhaustive
import press.widgets.Truss
import press.widgets.fromOreo
import press.widgets.setTextAndCursor
import press.widgets.textColor
import press.widgets.textSizePx

@SuppressLint("SetTextI18n")
class EditorView @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted openMode: EditorOpenMode,
  @Assisted private val navigator: Navigator,
  presenterFactory: EditorPresenter.Factory
) : ContourLayout(context) {

  private val toolbar = themed(Toolbar(context)).apply {
    navigationIcon = getDrawable(context, drawable.ic_close_24dp)
    themeAware {
      setBackgroundColor(it.window.editorBackgroundColor)
    }
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
    EditorUiStyles.editor.applyStyle(this)
    background = null
    breakStrategy = BREAK_STRATEGY_HIGH_QUALITY
    gravity = TOP
    inputType = TYPE_CLASS_TEXT or  // Multiline doesn't work without this.
        TYPE_TEXT_FLAG_CAP_SENTENCES or
        TYPE_TEXT_FLAG_MULTI_LINE or
        TYPE_TEXT_FLAG_NO_SUGGESTIONS
    updatePaddingRelative(start = 16.dip, end = 16.dip, bottom = 16.dip)
    CapitalizeOnHeadingStart.capitalize(this)
    fromOreo {
      importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
    }
    themeAware {
      textColor = it.textColorPrimary
    }
  }

  private val headingHintTextView = themed(TextView(context)).apply {
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

    themeAware { palette ->
      setBackgroundColor(palette.window.editorBackgroundColor)

      // TODO: avoid recreating Wysiwg on every
      //  theme change to share the same span-pool.
      val wysiwygTheme = WysiwygTheme(
          displayUnits = DisplayUnits(context),
          headingTextColor = palette.textColorHeading
      )
      val wysiwyg = Wysiwyg(editorEditText, wysiwygTheme)
      editorEditText.addTextChangedListener(wysiwyg.syntaxHighlighter())
    }

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
    if (model.hintText == null) {
      headingHintTextView.visibility = GONE
    } else {
      headingHintTextView.visibility = View.VISIBLE
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
    }
  }

  private fun render(uiUpdate: EditorUiUpdate) {
    when (uiUpdate) {
      is PopulateContent -> editorEditText.setTextAndCursor(uiUpdate.content)
      is CloseNote -> navigator.goTo(Back)
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
