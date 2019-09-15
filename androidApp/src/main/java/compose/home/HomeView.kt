package compose.home

import android.app.Activity
import android.content.Context
import android.view.animation.PathInterpolator
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.benasher44.uuid.Uuid
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import compose.editor.EditorActivity
import compose.editor.EditorView
import compose.theme.themeAware
import compose.theme.themed
import compose.util.heightOf
import compose.widgets.BackpressInterceptResult
import compose.widgets.BackpressInterceptResult.IGNORED
import compose.widgets.BackpressInterceptResult.INTERCEPTED
import compose.widgets.attr
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
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import timber.log.Timber

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
      if (it is ComposeNewNote) {
        openNewNoteScreen()
      }
    }

    newNoteClicks
        .uiModels(presenter.create(navigator))
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

    noteAdapter.onNoteClicked = { noteModel ->
      Timber.i("Note clicked: $noteModel. Inflating EditorView.")
      if (noteEditorPage.childCount != 0) error("Multiple EditorViews? :O")

      val editorView = createEditorView(noteModel.noteUuid)
      noteEditorPage.addView(editorView)
      noteEditorPage.addStateChangeCallbacks(
          ToggleKeyboardOnPageStateChange(editorView.editorEditText)
      )
      noteEditorPage.addStateChangeCallbacks(object : SimplePageStateChangeCallbacks() {
        override fun onPageCollapsed() {
          noteEditorPage.removeStateChangeCallbacks(this)
          noteEditorPage.removeView(editorView)
        }
      })

      noteEditorPage.post {
        notesList.expandItem(itemId = noteModel.adapterId)
      }
    }

    noteEditorPage.addStateChangeCallbacks(ToggleFabOnPageStateChange(newNoteFab))
    noteEditorPage.addStateChangeCallbacks(ToggleSoftInputModeOnPageStateChange(activity.window))
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

  fun offerBackPress(): BackpressInterceptResult {
    return if (noteEditorPage.isExpandedOrExpanding) {
      notesList.collapse()
      INTERCEPTED
    } else {
      IGNORED
    }
  }

  @AssistedInject.Factory
  interface Factory {
    fun withContext(context: Context): HomeView
  }
}