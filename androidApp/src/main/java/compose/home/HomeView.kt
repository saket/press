package compose.home

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.PathInterpolator
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.benasher44.uuid.Uuid
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import com.mikepenz.itemanimators.AlphaInAnimator
import com.soywiz.klock.seconds
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import compose.editor.EditorActivity
import compose.editor.EditorView
import compose.theme.themeAware
import compose.theme.themed
import compose.util.heightOf
import compose.util.throttleFirst
import compose.widgets.BackPressInterceptResult
import compose.widgets.BackPressInterceptResult.BACK_PRESS_IGNORED
import compose.widgets.BackPressInterceptResult.BACK_PRESS_INTERCEPTED
import compose.widgets.SpacingBetweenItemsDecoration
import compose.widgets.addStateChangeCallbacks
import compose.widgets.attr
import compose.widgets.locationOnScreen
import compose.widgets.suspendWhileExpanded
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.compose.R
import me.saket.compose.shared.editor.EditorOpenMode.ExistingNote
import me.saket.compose.shared.home.HomeEvent
import me.saket.compose.shared.home.HomeEvent.NewNoteClicked
import me.saket.compose.shared.home.HomePresenter
import me.saket.compose.shared.home.HomeUiModel
import me.saket.compose.shared.navigation.RealNavigator
import me.saket.compose.shared.navigation.ScreenKey.Back
import me.saket.compose.shared.navigation.ScreenKey.ComposeNewNote
import me.saket.compose.shared.uiModels
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.dimming.TintPainter
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.InterceptResult.IGNORED
import me.saket.inboxrecyclerview.page.InterceptResult.INTERCEPTED
import me.saket.inboxrecyclerview.page.OnPullToCollapseInterceptor

class HomeView @AssistedInject constructor(
  @Assisted context: Context,
  private val noteAdapter: NoteAdapter,
  private val presenter: HomePresenter.Factory,
  private val editorViewFactory: EditorView.Factory
) : ContourLayout(context) {

  private val activity = context as Activity

  private val notesList = themed(InboxRecyclerView(context)).apply {
    layoutManager = LinearLayoutManager(context)
    adapter = noteAdapter
    tintPainter = TintPainter.uncoveredArea()
    itemAnimator = AlphaInAnimator()
    addItemDecoration(SpacingBetweenItemsDecoration(2.dip))
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  private val toolbar = themed(Toolbar(context)).apply {
    setTitle(R.string.app_name)
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.heightOf(attr(android.R.attr.actionBarSize))
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
    notesList.expandablePage = this
    elevation = 20f.dip
    animationInterpolator = PathInterpolator(0.5f, 0f, 0f, 1f)
    pushParentToolbarOnExpand(toolbar)
    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.bottomTo { parent.bottom() }
    )
  }

  init {
    setupNoteEditorPage()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val newNoteClicks = newNoteFab
        .clicks()
        .map<HomeEvent> { NewNoteClicked }

    val navigator = RealNavigator {
      require(it is ComposeNewNote)
      openNewNoteScreen()
    }

    newNoteClicks.uiModels(presenter.create(navigator))
        .suspendWhileExpanded(noteEditorPage)
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(::render)
  }

  private fun setupNoteEditorPage() {
    val createEditorView = { noteUuid: Uuid ->
      val editorNavigator = RealNavigator { screen ->
        when (screen) {
          is Back -> notesList.collapse()
          else -> error("Unhandled $screen")
        }
      }
      val editorView = editorViewFactory.create(
          context = context,
          openMode = ExistingNote(noteUuid),
          navigator = editorNavigator
      )
      editorView
    }

    noteAdapter.noteClicks
        .throttleFirst(1.seconds, mainThread())
        .takeUntil(detaches())
        .subscribe { noteModel ->
          with(createEditorView(noteModel.noteUuid)) {
            noteEditorPage.addView(this)
            noteEditorPage.addStateChangeCallbacks(ToggleKeyboardOnPageStateChange(editorEditText))
            noteEditorPage.pullToCollapseInterceptor = interceptIfViewCanBeScrolled(scrollView)
          }
          noteEditorPage.post {
            notesList.expandItem(itemId = noteModel.adapterId)
          }
        }

    noteEditorPage.addStateChangeCallbacks(
        ToggleFabOnPageStateChange(newNoteFab),
        RemoveChildrenOnPageCollapse(noteEditorPage),
        ToggleSoftInputModeOnPageStateChange(activity.window)
    )
  }

  private fun interceptIfViewCanBeScrolled(view: View): OnPullToCollapseInterceptor {
    return { downX, downY, upwardPull ->
      val touchLiesOnView = view.locationOnScreen().contains(downX.toInt(), downY.toInt())

      if (touchLiesOnView) {
        val directionInt = if (upwardPull) +1 else -1
        val canScrollFurther = view.canScrollVertically(directionInt)
        if (canScrollFurther) INTERCEPTED else IGNORED
      } else {
        IGNORED
      }
    }
  }

  private fun render(model: HomeUiModel) {
    noteAdapter.submitList(model.notes)
  }

  private fun openNewNoteScreen() {
    val (intent, options) = EditorActivity.intentWithFabTransform(
        activity = activity,
        fab = newNoteFab,
        fabIconRes = R.drawable.ic_note_add_24dp
    )
    startActivity(context, intent, options.toBundle())
  }

  fun offerBackPress(): BackPressInterceptResult {
    return if (noteEditorPage.isExpandedOrExpanding) {
      notesList.collapse()
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
