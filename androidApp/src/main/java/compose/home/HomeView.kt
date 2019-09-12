package compose.home

import android.content.Context
import android.view.animation.PathInterpolator
import androidx.appcompat.widget.Toolbar
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
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
import compose.widgets.hideKeyboard
import compose.widgets.showKeyboard
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.compose.R
import me.saket.compose.shared.contentModels
import me.saket.compose.shared.home.HomeEvent
import me.saket.compose.shared.home.HomeEvent.NewNoteClicked
import me.saket.compose.shared.home.HomePresenter
import me.saket.compose.shared.home.HomeUiModel
import me.saket.compose.shared.navigation.RealNavigator
import me.saket.compose.shared.navigation.ScreenKey.Back
import me.saket.compose.shared.navigation.ScreenKey.NewNote
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.dimming.TintPainter
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks

class HomeView @AssistedInject constructor(
  @Assisted context: Context,
  private val noteAdapter: NoteAdapter,
  private val presenter: HomePresenter.Factory,
  private val editorViewFactory: EditorView.Factory
) : ContourLayout(context) {

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
      setBackgroundColor(it.windowTheme.backgroundColor)
    }
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.bottomTo { parent.bottom() }
    )
  }

  init {
    setupNoteEditorPage()

    noteEditorPage.addStateChangeCallbacks(ToggleFabOnPageStateChange(newNoteFab))
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val newNoteClicks = newNoteFab
        .clicks()
        .map<HomeEvent> { NewNoteClicked }

    val navigator = RealNavigator {
      if (it is NewNote) {
        openNewNoteScreen()
      }
    }

    newNoteClicks
        .contentModels(presenter.create(navigator))
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(::render)
  }

  private fun setupNoteEditorPage() {
    val createEditorView = {
      val editorNavigator = RealNavigator { screen ->
        when (screen) {
          is Back -> notesList.collapse()
          else -> error("Unhandled $screen")
        }
      }
      val editorView = editorViewFactory.create(context, editorNavigator)

      noteEditorPage.addStateChangeCallbacks(object : SimplePageStateChangeCallbacks() {
        override fun onPageAboutToExpand(expandAnimDuration: Long) {
          postDelayed(expandAnimDuration / 2) {
            editorView.editorEditText.showKeyboard()
          }
        }

        override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
          postDelayed(collapseAnimDuration / 2) {
            editorView.hideKeyboard()
          }
        }

        override fun onPageCollapsed() {
          noteEditorPage.removeView(editorView)
        }
      })

      editorView
    }

    noteAdapter.onNoteClicked = { noteModel ->
      val editorView = createEditorView()
      noteEditorPage.addView(editorView)
      noteEditorPage.post {
        notesList.expandItem(itemId = noteModel.adapterId)
      }
    }
  }

  private fun render(model: HomeUiModel) {
    noteAdapter.submitList(model.notes)
  }

  private fun openNewNoteScreen() {
    context.startActivity(EditorActivity.intent(context))
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