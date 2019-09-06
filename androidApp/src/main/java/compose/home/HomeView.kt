package compose.home

import android.content.Context
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import compose.ComposeApp
import compose.theme.autoApply
import compose.util.attr
import compose.util.heightOf
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.compose.R
import me.saket.compose.shared.contentModels
import me.saket.compose.shared.home.HomeEvent
import me.saket.compose.shared.home.HomeEvent.NewNoteClicked
import me.saket.compose.shared.home.HomePresenter
import me.saket.compose.shared.home.HomeUiModel
import me.saket.compose.shared.navigation.RealNavigator
import me.saket.compose.shared.navigation.ScreenKey.NewNote

class HomeView @AssistedInject constructor(
  @Assisted context: Context,
  private val presenter: HomePresenter.Factory,
  private val noteAdapter: NoteAdapter,
  private val style: Observable<HomeStyle>
) : ContourLayout(context) {

  private val toolbar = Toolbar(context).apply {
    style.map { it.toolbar }.autoApply(this)
    setTitle(R.string.app_name)
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.heightOf(attr(android.R.attr.actionBarSize))
    )
  }

  private val notesList = RecyclerView(context).apply {
    layoutManager = LinearLayoutManager(context)
    style.map { it.recyclerView }.autoApply(this)
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  private val newNoteFab = FloatingActionButton(context).apply {
    setImageResource(R.drawable.ic_note_add_24dp)
    style.map { it.newNoteFab }.autoApply(this)
    applyLayout(
        x = rightTo { parent.right() - 24.dip },
        y = bottomTo { parent.bottom() - 24.dip }
    )
  }

  init {
    ComposeApp.component.inject(this)
    notesList.adapter = noteAdapter
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

  private fun render(model: HomeUiModel) {
    noteAdapter.submitList(model.notes)
  }

  private fun openNewNoteScreen() {
    Toast.makeText(context, "Foo", Toast.LENGTH_SHORT).show()
  }

  @AssistedInject.Factory
  interface Factory {
    fun withContext(context: Context): HomeView
  }
}