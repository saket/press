package press.editor

import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import android.view.MenuItem.SHOW_AS_ACTION_NEVER
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.core.view.ViewCompat
import androidx.core.view.updatePaddingRelative
import app.cash.exhaustive.Exhaustive
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import me.saket.cascade.CascadeBackNavigator
import me.saket.cascade.CascadePopupMenu
import me.saket.cascade.overrideAllPopupMenus
import me.saket.press.R
import me.saket.press.shared.editor.EditorEffect
import me.saket.press.shared.editor.EditorEffect.BlockedDueToSyncConflict
import me.saket.press.shared.editor.EditorEffect.PopulateNoteBody
import me.saket.press.shared.editor.EditorEffect.ShowToast
import me.saket.press.shared.editor.EditorEvent.CloseSubMenu
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorModel
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.EditorPresenter.Args
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.editor.ToolbarIconKind.Archive
import me.saket.press.shared.editor.ToolbarIconKind.MoveToFolder
import me.saket.press.shared.editor.ToolbarIconKind.CopyAs
import me.saket.press.shared.editor.ToolbarIconKind.DeleteNote
import me.saket.press.shared.editor.ToolbarIconKind.DuplicateNote
import me.saket.press.shared.editor.ToolbarIconKind.OpenInSplitScreen
import me.saket.press.shared.editor.ToolbarIconKind.ShareAs
import me.saket.press.shared.editor.ToolbarIconKind.Unarchive
import me.saket.press.shared.editor.ToolbarMenuAction
import me.saket.press.shared.editor.ToolbarMenuItem
import me.saket.press.shared.editor.ToolbarSubMenu
import me.saket.press.shared.listenRx
import me.saket.press.shared.preferences.UserPreferences
import me.saket.press.shared.saveEditorContentOnClose
import me.saket.press.shared.theme.AppTheme
import me.saket.press.shared.theme.DisplayUnits
import me.saket.press.shared.theme.TextStyles.mainBody
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.theme.palettes.ThemePalette
import me.saket.press.shared.theme.palettes.wysiwygStyle
import me.saket.press.shared.ui.models
import me.saket.wysiwyg.Wysiwyg
import me.saket.wysiwyg.formatting.TextSelection
import me.saket.wysiwyg.parser.node.HeadingLevel.H1
import me.saket.wysiwyg.widgets.addTextChangedListener
import press.editor.format.EditorFormattingToolbar
import press.extensions.doOnEveryLayout
import press.extensions.doOnTextChange
import press.extensions.getDrawable
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
import press.theme.themePalette
import press.widgets.PressToolbar

class EditorView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  presenterFactory: EditorPresenter.Factory,
  preferences: UserPreferences,
  private val appTheme: AppTheme
) : ContourLayout(context), BackPressInterceptor {

  private val toolbar = PressToolbar(context).apply {
    setBackgroundColor(themePalette().window.elevatedBackgroundColor)
  }

  private val scrollView = EditorScrollView(context).apply {
    id = R.id.editor_scrollable_container
    isFillViewport = true
  }

  private val editorEditText = MarkdownEditText(context).apply {
    applyStyle(mainBody)
    id = R.id.editor_textfield
    textColor = themePalette().textColorPrimary
    if (preferences.autoCorrectEnabled.get()!!.enabled) {
      inputType = inputType or TYPE_TEXT_FLAG_AUTO_CORRECT
    }
    movementMethod = EditorLinkMovementMethod(scrollView)
    updatePaddingRelative(start = 20.dip, end = 20.dip, bottom = 52.dip)
  }

  private val headingHintTextView = TextView(context, mainBody).apply {
    textSizePx = editorEditText.textSize
    textColor = themePalette().textColorHint
  }

  private val formattingToolbar = EditorFormattingToolbar(editorEditText)

  private val presenter by unsafeLazy {
    presenterFactory.create(
      Args(
        openMode = screenKey<EditorScreenKey>().openMode,
        deleteBlankNewNoteOnExit = true,
        navigator = navigator(),
        onEffect = ::render
      )
    )
  }

  init {
    id = R.id.editor_view
    setBackgroundColor(themePalette().window.elevatedBackgroundColor)

    toolbar.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
    headingHintTextView.layoutBy(
      x = leftTo { scrollView.left() + editorEditText.paddingStart }
        .rightTo { scrollView.right() - editorEditText.paddingStart },
      y = topTo { scrollView.top() + editorEditText.paddingTop }
    )
    scrollView.layoutBy(
      x = matchParentX(),
      y = topTo { toolbar.bottom() }.bottomTo { formattingToolbar.top() }
    )
    formattingToolbar.layoutBy(
      x = matchParentX(),
      y = bottomTo { parent.bottom() }
    )

    scrollView.addView(editorEditText, MATCH_PARENT, WRAP_CONTENT)
    formattingToolbar.doOnEveryLayout {
      scrollView.setFadingEdgeLength(formattingToolbar.height * 3 / 4)
    }
    ViewCompat.setWindowInsetsAnimationCallback(
      scrollView,
      KeepCursorVisibleOnKeyboardShow(scrollView, editorEditText)
    )

    val wysiwygStyle = themePalette().wysiwygStyle(DisplayUnits(context))
    val wysiwyg = Wysiwyg(editorEditText, wysiwygStyle)
    editorEditText.addTextChangedListener(wysiwyg.syntaxHighlighter())

    if (screenKey<EditorScreenKey>().openMode is NewNote) {
      editorEditText.post {
        editorEditText.showKeyboard()
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    editorEditText.doOnTextChange {
      presenter.dispatch(NoteTextChanged(it.toString()))
    }

    presenter.models()
      .observeOn(mainThread())
      .publishAndConnect { models ->
        models
          .takeUntil(detaches())
          .subscribe(::render)

        models.map { it.toolbarMenu }
          .distinctUntilChanged()
          .let { Observables.combineLatest(it, appTheme.listenRx()) }
          .takeUntil(detaches())
          .subscribe { (menu, palette) ->
            renderToolbarMenu(menu, palette)
          }
      }
  }

  @CheckReturnValue
  fun <T> Observable<T>.publishAndConnect(func: (Observable<T>) -> Unit): Disposable {
    return publish().also { func.invoke(it) }.connect()
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

  private fun render(model: EditorModel) {
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

  private fun render(effect: EditorEffect) {
    mainThread().scheduleDirect {
      @Exhaustive
      when (effect) {
        is PopulateNoteBody -> editorEditText.setText(effect.newText, effect.newSelection)
        is BlockedDueToSyncConflict -> EditingBlockedDueToConflictDialog.show(context, onDismiss = navigator()::goBack)
        is ShowToast -> Toast.makeText(context, effect.message, LENGTH_SHORT).show()
      }
    }
  }

  private fun renderToolbarMenu(
    items: List<ToolbarMenuItem>,
    palette: ThemePalette
  ) {
    toolbar.overflowIcon!!.setTint(palette.accentColor)

    val backNavigator = CascadeBackNavigator()
    toolbar.menu.clear()
    for (item in items) {
      item.addToMenu(toolbar.menu, palette, backNavigator)
    }

    toolbar.overrideAllPopupMenus { context, anchor ->
      CascadePopupMenu(
        context = context,
        anchor = anchor,
        styler = pressCascadeStyler(),
        backNavigator = backNavigator
      )
    }
  }

  private fun ToolbarMenuItem.addToMenu(
    menu: Menu,
    palette: ThemePalette,
    backNavigator: CascadeBackNavigator
  ) {
    val item: ToolbarMenuItem = this
    val iconRes = when (item.icon) {
      MoveToFolder -> R.drawable.ic_twotone_folder_24
      Archive -> R.drawable.ic_twotone_archive_24
      Unarchive -> R.drawable.ic_twotone_unarchive_24
      ShareAs -> R.drawable.ic_twotone_share_24
      CopyAs -> R.drawable.ic_twotone_file_copy_24
      DuplicateNote -> R.drawable.ic_twotone_note_add_24
      OpenInSplitScreen -> R.drawable.ic_twotone_vertical_split_24
      DeleteNote -> R.drawable.ic_twotone_delete_24
      null -> null
    }
    val icon = iconRes?.let { context.getDrawable(iconRes, palette.accentColor) }

    val menuItem = when (item) {
      is ToolbarMenuAction -> {
        menu.add(item.coloredLabel(palette)).setOnMenuItemClickListener {
          when (item.clickEvent) {
            is CloseSubMenu -> backNavigator.navigateBack()
            else -> presenter.dispatch(item.clickEvent)
          }
          true
        }
      }
      is ToolbarSubMenu -> {
        menu.addSubMenu(item.label)
          .setHeaderTitle(item.subMenuTitle)
          .also { subMenu ->
            item.children.forEach { it.addToMenu(subMenu, palette, backNavigator) }
          }
          .item
      }
    }

    menuItem.also {
      it.icon = icon
      it.iconTintList = ColorStateList.valueOf(palette.accentColor)
      it.setShowAsAction(if (menu.size() <= 2) SHOW_AS_ACTION_IF_ROOM else SHOW_AS_ACTION_NEVER)
    }
  }
}

private fun ToolbarMenuAction.coloredLabel(palette: ThemePalette): CharSequence {
  return if (isDangerousAction) {
    buildSpannedString {
      color(palette.textColorWarning) {
        append(label)
      }
    }
  } else {
    label
  }
}

private fun EditText.setText(newText: CharSequence, newSelection: TextSelection?) {
  setText(newText)
  newSelection?.let {
    setSelection(it.start, it.end)
  }
}
