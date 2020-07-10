package press.home

import android.app.Activity
import android.content.Context
import android.graphics.Color.BLACK
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import android.view.animation.PathInterpolator
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.detaches
import com.mikepenz.itemanimators.AlphaInAnimator
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.subjects.BehaviorSubject
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.dimming.TintPainter
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.press.R
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.home.ComposeNewNote
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.home.HomeEvent.WindowFocusChanged
import me.saket.press.shared.home.HomePresenter
import me.saket.press.shared.home.HomePresenter.Args
import me.saket.press.shared.home.HomeUiModel
import me.saket.press.shared.localization.strings
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.editor.EditorActivity
import press.editor.EditorView
import press.extensions.attr
import press.extensions.doOnAttach
import press.extensions.getDrawable
import press.extensions.heightOf
import press.extensions.hideKeyboard
import press.extensions.parentView
import press.extensions.second
import press.extensions.suspendWhile
import press.extensions.throttleFirst
import press.handle
import press.navigator
import press.sync.PreferencesActivity
import press.theme.themeAware
import press.theme.themed
import press.widgets.BackPressInterceptResult
import press.widgets.BackPressInterceptResult.BACK_PRESS_IGNORED
import press.widgets.BackPressInterceptResult.BACK_PRESS_INTERCEPTED
import press.widgets.SpacingBetweenItemsDecoration
import press.widgets.addStateChangeCallbacks
import press.widgets.doOnNextAboutToCollapse
import press.widgets.doOnNextCollapse
import press.widgets.interceptPullToCollapseOnView
import press.widgets.suspendWhileExpanded

class HomeView @AssistedInject constructor(
  @Assisted context: Context,
  private val noteAdapter: NoteAdapter,
  private val presenter: HomePresenter.Factory,
  private val editorViewFactory: EditorView.Factory
) : ContourLayout(context) {

  private val activity = context as Activity
  private val windowFocusChanges = BehaviorSubject.createDefault(WindowFocusChanged(hasFocus = true))

  private val toolbar = themed(Toolbar(context)).apply {
    setTitle(R.string.app_name)
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.heightOf(attr(android.R.attr.actionBarSize))
    )
  }

  private val notesList = themed(InboxRecyclerView(context)).apply {
    id = R.id.home_notes
    layoutManager = LinearLayoutManager(context)
    adapter = noteAdapter
    tintPainter = TintPainter.uncoveredArea(color = BLACK, opacity = 0.25f)
    itemAnimator = AlphaInAnimator()
    doOnAttach {
      // Let the items draw over the toolbar.
      parentView.clipChildren = false
    }
    addItemDecoration(SpacingBetweenItemsDecoration(1.dip))
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  private val newNoteFab = themed(FloatingActionButton(context)).apply {
    setImageResource(R.drawable.ic_note_add_24dp)
    applyLayout(
        x = rightTo { parent.right() - 24.dip },
        y = bottomTo { parent.bottom() - 24.dip }
    )
  }

  private val noteEditorPage = ExpandablePageLayout(context).apply {
    id = R.id.home_editor
    notesList.expandablePage = this
    elevation = 20f.dip
    animationInterpolator = PathInterpolator(0.5f, 0f, 0f, 1f)
    animationDurationMillis = 350
    pushParentToolbarOnExpand(toolbar)
    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.bottomTo { parent.bottom() }
    )
  }

  private var activeNote: ActiveNote? = null
    set(note) {
      field = note
      if (note == null) {
        notesList.collapse()
      } else {
        val editorView = editorViewFactory.create(
            context = context,
            openMode = ExistingNote(note.noteId),
            onDismiss = notesList::collapse
        )
        noteEditorPage.addView(editorView)
        noteEditorPage.doOnNextCollapse { it.removeView(editorView) }
        noteEditorPage.pullToCollapseInterceptor = interceptPullToCollapseOnView(editorView.scrollView)
      }
    }

  init {
    id = R.id.home_view
    setupNoteEditorPage()

    themeAware { palette ->
      toolbar.menu.clear()
      toolbar.menu.add(
          icon = context.getDrawable(R.drawable.ic_preferences_24dp, palette.accentColor),
          title = context.strings().home.preferences,
          onClick = { context.startActivity(PreferencesActivity.intent(context)) }
      )
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val presenter = presenter.create(Args(
        includeEmptyNotes = false,
        navigator = navigator().handle<ComposeNewNote> {
          openNewNoteScreen(it.noteId)
        }
    ))
    presenter.uiUpdates()
        // These two suspend calls skip updates while an
        // existing note or the new-note screen is open.
        .suspendWhileExpanded(noteEditorPage)
        .suspendWhile(windowFocusChanges) { it.hasFocus.not() }
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(::render)

    newNoteFab.setOnClickListener {
      presenter.dispatch(NewNoteClicked)
    }
  }

  override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
    super.onWindowFocusChanged(hasWindowFocus)
    windowFocusChanges.onNext(WindowFocusChanged(hasWindowFocus))
  }

  override fun onSaveInstanceState(): Parcelable? {
    return HomeViewSavedState(
        superState = super.onSaveInstanceState(),
        activeNote = activeNote
    )
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    require(state is HomeViewSavedState)

    // InboxRecyclerView is capable of restoring its own state. We
    // only need to ensure that the EditorView is ready to be shown.
    activeNote = state.activeNote

    super.onRestoreInstanceState(state.superState)
  }

  private fun setupNoteEditorPage() {
    noteAdapter.noteClicks
        .throttleFirst(1.second, mainThread())
        .takeUntil(detaches())
        .subscribe { note ->
          activeNote = note.toActiveNote()
          noteEditorPage.post {
            notesList.expandItem(itemId = note.adapterId)
          }
        }

    noteEditorPage.doOnNextCollapse {
      activeNote = null
    }

    noteEditorPage.doOnNextAboutToCollapse { collapseAnimDuration ->
      postDelayed(collapseAnimDuration / 2) {
        hideKeyboard()
      }
    }

    noteEditorPage.addStateChangeCallbacks(
        ToggleFabOnPageStateChange(newNoteFab),
        ToggleSoftInputModeOnPageStateChange(activity.window)
    )
  }

  private fun render(model: HomeUiModel) {
    noteAdapter.submitList(model.notes)
  }

  private fun openNewNoteScreen(noteId: NoteId) {
    val (intent, options) = EditorActivity.intentWithFabTransform(
        activity = activity,
        openMode = ExistingNote(noteId),
        fab = newNoteFab,
        fabIconRes = R.drawable.ic_note_add_24dp
    )
    ContextCompat.startActivity(context, intent, options.toBundle())
  }

  fun offerBackPress(): BackPressInterceptResult {
    return if (noteEditorPage.isExpandedOrExpanding) {
      activeNote = null
      BACK_PRESS_INTERCEPTED
    } else {
      BACK_PRESS_IGNORED
    }
  }

  @AssistedInject.Factory
  interface Factory {
    fun withContext(context: Context): HomeView
  }
}

private fun Menu.add(icon: Drawable, title: String, onClick: () -> Unit) {
  add(title).let {
    it.icon = icon
    it.setShowAsAction(SHOW_AS_ACTION_IF_ROOM)
    it.setOnMenuItemClickListener { onClick(); true }
  }
}
