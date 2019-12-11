@file:Suppress("unused")

package press.editor

import android.content.Context
import android.graphics.Color
import android.graphics.Color.WHITE
import android.os.Bundle
import android.os.Parcelable
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
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePaddingRelative
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.widget.textChanges
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.R
import me.saket.press.R.drawable
import me.saket.press.shared.editor.EditorEvent
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorOpenMode
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.EditorPresenter.Args
import me.saket.press.shared.editor.EditorUiEffect
import me.saket.press.shared.editor.EditorUiEffect.CloseNote
import me.saket.press.shared.editor.EditorUiEffect.PopulateContent
import me.saket.press.shared.editor.EditorUiModel
import me.saket.press.shared.subscribe
import me.saket.press.shared.theme.DisplayUnits
import me.saket.press.shared.theme.EditorUiStyles
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.theme.from
import me.saket.press.shared.uiUpdates
import me.saket.wysiwyg.Wysiwyg
import me.saket.wysiwyg.parser.node.HeadingLevel.H1
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.addTextChangedListener
import press.theme.themeAware
import press.theme.themePalette
import press.theme.themed
import press.util.exhaustive
import press.widgets.Truss
import press.widgets.fromOreo
import press.widgets.setText
import press.widgets.textColor
import press.widgets.textSizePx

class EditorView @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted openMode: EditorOpenMode,
  @Assisted private val onDismiss: () -> Unit,
  presenterFactory: EditorPresenter.Factory
) : ContourLayout(context) {

  companion object {
    private const val KEY_SUPER = "press::key::super"
    private const val KEY_SCROLL_POSITION = "press::key::scroll_position"
    private const val KEY_CONTENT_WIDTH = "press::key::aspect_ratio"
  }

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
    movementMethod = EditorLinkMovementMethod(scrollView)
    updatePaddingRelative(start = 16.dip, end = 16.dip, bottom = 16.dip)
    doOnLayout {
      contentWidth = layout.width
    }
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

  private var contentWidth: Int = 0

  private val presenter = presenterFactory.create(Args(openMode))

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
      // TODO: detect if the keyboard is up and delay going back by
      //  a bit so that the IRV behind is resized before this View
      //  start collapsing.
      onDismiss()
    }
  }

  override fun onSaveInstanceState(): Parcelable? {
    val bundle = Bundle()
    val scrollPosition = scrollView.scrollY
    bundle.putInt(KEY_SCROLL_POSITION, scrollPosition)
    bundle.putInt(KEY_CONTENT_WIDTH, contentWidth)
    super.onSaveInstanceState()
        ?.let {
          bundle.putParcelable(KEY_SUPER, it)
        }
    return bundle
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    var savedState = state
    if (savedState is Bundle) {
      val scrollPosition = savedState.getInt(KEY_SCROLL_POSITION)
      scrollView.setTag(R.id.scrollStatePosition, scrollPosition)
      val oldWidth: Int = savedState.getInt(KEY_CONTENT_WIDTH)
      scrollView.setTag(R.id.scrollStateContentWidth, oldWidth)

      savedState = savedState.getParcelable(KEY_SUPER)
    }
    super.onRestoreInstanceState(savedState)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val noteTextChanges: Observable<EditorEvent> = editorEditText
        .textChanges()
        .map { NoteTextChanged(it.toString()) }

    Observable.mergeArray(noteTextChanges)
        .uiUpdates(presenter)
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(models = ::render, effects = ::render)
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
          .append("# ")
          .popSpan()
          .append(model.hintText ?: "")
          .popSpan()
          .build()
    }

    onRendered()
  }

  private fun render(uiUpdate: EditorUiEffect) {
    when (uiUpdate) {
      is PopulateContent -> editorEditText.setText(
          uiUpdate.content, moveCursorToEnd = uiUpdate.moveCursorToEnd
      )
      is CloseNote -> onDismiss()
    }.exhaustive

    onRendered()
  }

  private fun onRendered() {
    val scrollPosition = scrollView.getTag(R.id.scrollStatePosition)
    val oldWidth = scrollView.getTag(R.id.scrollStateContentWidth)
    if (scrollPosition is Int && oldWidth is Int) {
      scrollView.setTag(R.id.scrollStatePosition, Any())
      scrollView.setTag(R.id.scrollStateContentWidth, Any())

      scrollView.doOnNextLayout {
        scrollView.post {
          val currentWidth = editorEditText.layout.width

          val toScroll = run {
            if (oldWidth == currentWidth) {
              scrollPosition
            } else {
              scrollPosition * oldWidth / currentWidth
            }
          }
          scrollView.scrollTo(0, toScroll)
        }
      }
    }
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(
      context: Context,
      openMode: EditorOpenMode,
      onDismiss: () -> Unit
    ): EditorView
  }
}
