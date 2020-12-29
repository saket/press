package press.editor

import android.content.Context
import android.content.res.ColorStateList
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
import android.widget.Toast
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.updatePaddingRelative
import androidx.core.widget.NestedScrollView
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers
import me.saket.cascade.CascadePopupMenu
import me.saket.cascade.allChildren
import me.saket.cascade.overrideAllPopupMenus
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.press.R
import me.saket.press.shared.editor.AutoCorrectEnabled
import me.saket.press.shared.editor.EditorEvent
import me.saket.press.shared.editor.EditorEvent.ArchiveToggleClicked
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.EditorPresenter.Args
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.editor.EditorUiEffect
import me.saket.press.shared.editor.EditorUiEffect.BlockedDueToSyncConflict
import me.saket.press.shared.editor.EditorUiEffect.UpdateNoteText
import me.saket.press.shared.editor.EditorUiModel
import me.saket.press.shared.editor.saveEditorContentOnClose
import me.saket.press.shared.localization.strings
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
import press.Clipboards
import press.Intents
import press.editor.TextFormat.Html
import press.editor.TextFormat.Markdown
import press.extensions.doOnTextChange
import press.extensions.findParentOfType
import press.extensions.fromOreo
import press.extensions.interceptPullToCollapseOnView
import press.extensions.showKeyboard
import press.extensions.textColor
import press.extensions.textSizePx
import press.extensions.unsafeLazy
import press.navigation.BackPressInterceptor
import press.navigation.BackPressInterceptor.InterceptResult
import press.navigation.BackPressInterceptor.InterceptResult.Ignored
import press.navigation.navigator
import press.navigation.screenKey
import press.theme.pressCascadeStyler
import press.theme.themeAware
import press.theme.themePalette
import press.widgets.PressToolbar

class EditorView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  presenterFactory: EditorPresenter.Factory,
  autoCorrectEnabled: Setting<AutoCorrectEnabled>
) : ContourLayout(context), BackPressInterceptor {

  private val toolbar = PressToolbar(context).apply {
    themeAware {
      setBackgroundColor(it.window.editorBackgroundColor)
    }
    applyLayout(
      x = leftTo { parent.left() }.rightTo { parent.right() },
      y = topTo { parent.top() }
    )
  }

  private val scrollView = NestedScrollView(context).apply {
    id = R.id.editor_scrollable_container
    isFillViewport = true
    applyLayout(
      x = leftTo { parent.left() }.rightTo { parent.right() },
      y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  private val editorEditText = PlainTextPasteEditText(context).apply {
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
    updatePaddingRelative(start = 20.dip, end = 20.dip, bottom = 80.dip)
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

  private val presenter by unsafeLazy {
    presenterFactory.create(
      Args(
        openMode = screenKey<EditorScreenKey>().openMode,
        deleteBlankNewNoteOnExit = true,
        navigator = navigator()
      )
    )
  }

  private val toolbarArchiveItem by unsafeLazy { toolbar.menu.findItem(R.id.editortoolbar_archive) }
  private val toolbarUnarchiveItem by unsafeLazy { toolbar.menu.findItem(R.id.editortoolbar_unarchive) }
  private lateinit var toolbarMenuEvents: (EditorEvent) -> Unit

  init {
    id = R.id.editor_view
    scrollView.addView(editorEditText, MATCH_PARENT, WRAP_CONTENT)
    bringChildToFront(scrollView)
    themeAware { palette ->
      setBackgroundColor(palette.window.editorBackgroundColor)
    }

    populateToolbarMenu()

    // TODO: add support for changing WysiwygStyle.
    themePalette()
      .take(1)
      .takeUntil(detaches())
      .subscribe { palette ->
        val wysiwygStyle = WysiwygStyle.from(palette.markdown, DisplayUnits(context))
        val wysiwyg = Wysiwyg(editorEditText, wysiwygStyle)
        editorEditText.addTextChangedListener(wysiwyg.syntaxHighlighter())
      }

    if (screenKey<EditorScreenKey>().openMode is NewNote) {
      editorEditText.post {
        editorEditText.showKeyboard()
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val page = findParentOfType<ExpandablePageLayout>()
    page?.pullToCollapseInterceptor = interceptPullToCollapseOnView(scrollView)

    editorEditText.doOnTextChange {
      presenter.dispatch(NoteTextChanged(it.toString()))
    }
    toolbarMenuEvents = {
      presenter.dispatch(it)
    }

    presenter.uiUpdates()
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe(models = ::render, effects = ::render)
  }

  override fun onInterceptBackPress(): InterceptResult {
    // The content must only be saved when this screen is closed by the user.
    // Press previously saved content in onDetachedFromWindow(), but that caused
    // the note to get deleted if the note was empty even if the Activity was
    // being recreated, say, due to a theme change.
    presenter.saveEditorContentOnClose(editorEditText.text.toString())
      .subscribeOn(Schedulers.io())
      .subscribe()
    return Ignored
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

    toolbarArchiveItem.isVisible = !model.isArchived
    toolbarUnarchiveItem.isVisible = model.isArchived
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

  private fun populateToolbarMenu() {
    toolbar.inflateMenu(R.menu.editor_toolbar)
    val menuItems = toolbar.menu.allChildren

    val strings = context.strings().editor
    menuItems.forEach { item ->
      item.title = when (item.itemId) {
        R.id.editortoolbar_archive -> strings.menu_archive
        R.id.editortoolbar_unarchive -> strings.menu_unarchive
        R.id.editortoolbar_share_as -> strings.menu_share_as
        R.id.editortoolbar_share_as_markdown -> strings.menu_share_as_markdown
        R.id.editortoolbar_share_as_html -> strings.menu_share_as_html
        R.id.editortoolbar_copy_as -> strings.menu_copy_as
        R.id.editortoolbar_copy_as_markdown -> strings.menu_copy_as_markdown
        R.id.editortoolbar_copy_as_html -> strings.menu_copy_as_html
        R.id.editortoolbar_duplicate -> strings.menu_duplicate_note
        else -> error("No title for ${resources.getResourceEntryName(item.itemId)}")
      }
    }

    themeAware { palette ->
      toolbar.overflowIcon!!.setTint(palette.accentColor)
      menuItems.forEach { it.iconTintList = ColorStateList.valueOf(palette.accentColor) }

      toolbar.overrideAllPopupMenus { context, anchor ->
        CascadePopupMenu(context, anchor, styler = pressCascadeStyler(palette))
      }
    }

    toolbar.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.editortoolbar_archive -> {
          Toast.makeText(context, strings.note_archived, Toast.LENGTH_SHORT).show()
          toolbarMenuEvents(ArchiveToggleClicked(archive = true))
        }
        R.id.editortoolbar_unarchive -> {
          Toast.makeText(context, strings.note_unarchived, Toast.LENGTH_SHORT).show()
          toolbarMenuEvents(ArchiveToggleClicked(archive = false))
        }
        R.id.editortoolbar_share_as_markdown -> shareNoteAs(Markdown)
        R.id.editortoolbar_copy_as_markdown -> copyNoteAs(Markdown)
        else -> {
          if (!item.hasSubMenu()) {
            Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show()
          }
        }
      }
      true
    }
  }

  private fun shareNoteAs(format: TextFormat) {
    when (format) {
      Markdown -> {
        Intents.sharePlainText(context, format.generateFrom(editorEditText.text))
      }
      Html -> TODO("Convert markdown to HTML")
    }
  }

  private fun copyNoteAs(format: TextFormat) {
    when (format) {
      Markdown -> {
        Clipboards.copyPlainText(context, format.generateFrom(editorEditText.text).toString())
      }
      Html -> TODO("Convert markdown to HTML")
    }
  }
}
