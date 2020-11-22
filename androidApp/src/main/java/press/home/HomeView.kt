package press.home

import android.content.Context
import android.graphics.Color.BLACK
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.inboxrecyclerview.ExpandedItemFinder
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.dimming.DimPainter
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.press.R
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.home.HomePresenter
import me.saket.press.shared.home.HomePresenter.Args
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.home.HomeUiModel
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.SyncPreferencesScreenKey
import me.saket.press.shared.ui.ScreenKey
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.extensions.attr
import press.extensions.findParentOfType
import press.extensions.getDrawable
import press.extensions.heightOf
import press.extensions.interceptPullToCollapseOnView
import press.extensions.second
import press.extensions.throttleFirst
import press.navigation.ScreenFocusChangeListener
import press.navigation.navigator
import press.navigation.screenKey
import press.navigation.transitions.ExpandableScreenHost
import press.theme.themeAware
import press.widgets.DividerItemDecoration
import press.widgets.PressToolbar
import press.widgets.SlideDownItemAnimator

// TODO: Rename to NoteListView
class HomeView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  private val presenter: HomePresenter.Factory
) : ContourLayout(context), ScreenFocusChangeListener, ExpandableScreenHost {

  private val noteAdapter = NoteAdapter()
  private val folderAdapter = FolderListAdapter()
  private val screenKey = screenKey<HomeScreenKey>()

  private val toolbar = PressToolbar(context, showNavIcon = screenKey.folder != null).apply {
    applyLayout(
      x = leftTo { parent.left() }.rightTo { parent.right() },
      y = topTo { parent.top() }.heightOf(attr(android.R.attr.actionBarSize))
    )
  }

  private val notesList = InboxRecyclerView(context).apply {
    id = R.id.home_notes
    layoutManager = LinearLayoutManager(context)
    dimPainter = DimPainter.listAndPage(color = BLACK, alpha = 0.25f)
    toolbar.doOnLayout {
      clipToPadding = true  // for dimming to be drawn over the toolbar.
      updatePadding(top = toolbar.bottom)
    }
    itemAnimator = SlideDownItemAnimator()
    addItemDecoration(DividerItemDecoration())
    applyLayout(
      x = leftTo { parent.left() }.rightTo { parent.right() },
      y = topTo { parent.top() }.bottomTo { parent.bottom() }
    )
  }

  private val newNoteFab = FloatingActionButton(context).apply {
    setImageResource(R.drawable.ic_note_add_24dp)
    applyLayout(
      x = rightTo { parent.right() - 24.dip },
      y = bottomTo { parent.bottom() - 24.dip }
    )
  }

  init {
    id = R.id.home_view
    notesList.adapter = ConcatAdapter(folderAdapter, noteAdapter)

    themeAware { palette ->
      toolbar.menu.clear()
      toolbar.menu.add(
        icon = context.getDrawable(R.drawable.ic_preferences_24dp, palette.accentColor),
        title = context.strings().home.preferences,
        onClick = { navigator().lfg(SyncPreferencesScreenKey) }
      )
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

    presenter.uiUpdates()
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe(::render)

    newNoteFab.setOnClickListener {
      presenter.dispatch(NewNoteClicked)
    }

    Observable.merge(noteAdapter.clicks, folderAdapter.clicks)
      .throttleFirst(1.second, mainThread())
      .takeUntil(detaches())
      .subscribe { row ->
        navigator().lfg(row.screenKey())
      }

    val page = findParentOfType<ExpandablePageLayout>()
    page?.pullToCollapseInterceptor = interceptPullToCollapseOnView(notesList)
  }

  override fun identifyExpandingItem(): ExpandedItemFinder? {
    return ExpandedItemFinder { parent, id ->
      when (id) {
        is EditorScreenKey -> {
          (id.openMode as? ExistingNote)?.let {
            noteAdapter.findExpandedItem(parent, it.noteId.id)
          }
        }
        is HomeScreenKey -> {
          id.folder?.let {
            folderAdapter.findExpandedItem(parent, it)
          }
        }
        else -> null
      }
    }
  }

  override fun onScreenFocusChanged(focusedScreen: View?) {
    if (this === focusedScreen) {
      newNoteFab.show()
    } else {
      val focusedKey = focusedScreen?.screenKey<ScreenKey>()
      if ((focusedKey as? EditorScreenKey)?.openMode is ExistingNote) {
        newNoteFab.hide()
      }
    }
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    // The note list is positioned in front of the toolbar so that its items can go
    // over it, but RV steals all touch events even if there isn't a child under to
    // receive the event.
    return if (ev.y > toolbar.y && ev.y < (toolbar.y + toolbar.height)) {
      toolbar.dispatchTouchEvent(ev)
    } else {
      super.dispatchTouchEvent(ev)
    }
  }

  private fun render(model: HomeUiModel) {
    toolbar.title = model.title
    noteAdapter.submitList(model.notes)
    folderAdapter.submitList(model.folders)
  }
}

private fun Menu.add(icon: Drawable, title: String, onClick: () -> Unit) {
  add(title).let {
    it.icon = icon
    it.setShowAsAction(SHOW_AS_ACTION_IF_ROOM)
    it.setOnMenuItemClickListener { onClick(); true }
  }
}
