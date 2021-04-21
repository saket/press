package press.home

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.State
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.expander.InboxItemExpander
import me.saket.press.R
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.home.HomeEvent.SearchTextChanged
import me.saket.press.shared.home.HomeModel
import me.saket.press.shared.home.HomePresenter
import me.saket.press.shared.home.HomePresenter.Args
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.ui.ScreenKey
import me.saket.press.shared.ui.models
import press.extensions.doOnTextChange
import press.extensions.hideKeyboard
import press.extensions.second
import press.extensions.throttleFirst
import press.navigation.BackPressInterceptor
import press.navigation.BackPressInterceptor.InterceptResult
import press.navigation.ScreenFocusChangeListener
import press.navigation.navigator
import press.navigation.screenKey
import press.navigation.transitions.ExpandableScreenHost
import press.widgets.DividerItemDecoration
import press.widgets.SlideDownItemAnimator
import press.widgets.insets.keyboardHeight

class HomeView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  private val presenter: HomePresenter.Factory
) : ContourLayout(context), ScreenFocusChangeListener, ExpandableScreenHost, BackPressInterceptor {

  private val noteAdapter = NoteAdapter()
  private val folderAdapter = FolderListAdapter()
  private val screenKey = screenKey<HomeScreenKey>()

  override val toolbar = HomeToolbar(
    context = context,
    showNavIcon = !HomeScreenKey.isRoot(screenKey)
  )

  private val emptyStateView = EmptyStateView(context)

  private val notesList = InboxRecyclerView(context).apply {
    id = R.id.home_notes
    itemAnimator = SlideDownItemAnimator()
    addItemDecoration(DividerItemDecoration())
  }

  private val newNoteFab = FloatingActionButton(context).apply {
    setImageResource(R.drawable.ic_note_add_24dp)
  }

  init {
    id = R.id.home_view

    toolbar.layoutBy(
      x = leftTo { parent.left() }.rightTo { parent.right() },
      y = topTo { parent.top() }
    )
    emptyStateView.layoutBy(
      x = matchParentX(),
      y = centerVerticallyTo { parent.centerY() }
    )
    notesList.layoutBy(
      x = leftTo { parent.left() }.rightTo { parent.right() },
      y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
    newNoteFab.layoutBy(
      x = rightTo { parent.right() - 24.dip },
      y = bottomTo { parent.bottom() - 24.dip }
    )

    notesList.adapter = ConcatAdapter(folderAdapter, noteAdapter)
    notesList.layoutManager = object : LinearLayoutManager(context) {
      override fun calculateExtraLayoutSpace(state: State, extraLayoutSpace: IntArray) {
        super.calculateExtraLayoutSpace(state, extraLayoutSpace)
        // When this screen gets resized by the keyboard, we wanna continue showing items in the space covered
        // by the keyboard. This way all the notes stay visible when the editor screen is dragged for a note
        // item that's covered by the keyboard.
        extraLayoutSpace[1] = maxOf(keyboardHeight() ?: 0, extraLayoutSpace[1])
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val presenter = presenter.create(
      Args(
        screenKey = screenKey(),
        includeBlankNotes = false,
        navigator = navigator()
      )
    )

    presenter.models()
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe(::render)

    newNoteFab.setOnClickListener {
      toolbar.setSearchVisible(false, withKeyboard = false)
      presenter.dispatch(NewNoteClicked)
    }
    toolbar.searchField.doOnTextChange {
      notesList.scrollToPosition(0)
      presenter.dispatch(SearchTextChanged(text = it.toString()))
    }

    Observable.merge(noteAdapter.clicks, folderAdapter.clicks)
      .throttleFirst(1.second, mainThread())
      .takeUntil(detaches())
      .subscribe { row ->
        if (toolbar.isSearchVisible()) {
          hideKeyboard()
        }
        navigator().lfg(row.screenKey())
      }
  }

  override fun createScreenExpander(): InboxItemExpander<ScreenKey> {
    return InboxItemExpander { screen, viewHolders ->
      when (screen) {
        is EditorScreenKey -> {
          (screen.openMode as? ExistingNote)?.let { mode ->
            noteAdapter.viewHolderFor(mode.noteId.id, viewHolders)
          }
        }
        is HomeScreenKey -> {
          screen.folder?.let { folder ->
            folderAdapter.viewHolderFor(folder, viewHolders)
          }
        }
        else -> null
      }
    }
  }

  override fun onScreenFocusChanged(focusedScreen: ScreenKey?) {
    if (focusedScreen is EditorScreenKey && focusedScreen.openMode is ExistingNote) {
      // Hide the FAB only if an existing note is being opened.
      // If it's a new note, the FAB will morph into the new screen.
      newNoteFab.hide()
    } else {
      newNoteFab.show()
    }
  }

  override fun onInterceptBackPress(): InterceptResult {
    return if (toolbar.isSearchVisible()) {
      toolbar.setSearchVisible(false)
      InterceptResult.Intercepted
    } else {
      InterceptResult.Ignored
    }
  }

  private fun render(model: HomeModel) {
    toolbar.render(model)
    noteAdapter.submitList(model.notes)
    folderAdapter.submitList(model.folders)

    TransitionManager.beginDelayedTransition(this, Fade().addTarget(emptyStateView))
    emptyStateView.isVisible = model.emptyState != null
    emptyStateView.render(model.emptyState)
  }
}
