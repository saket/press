package compose.home

import android.content.Context
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import compose.editor.EditorActivity
import compose.theme.themeAware
import compose.theme.themed
import compose.util.heightOf
import compose.widgets.attr
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
  private val noteAdapter: NoteAdapter
) : ContourLayout(context) {

  private val toolbar = themed(Toolbar(context)).apply {
    setTitle(R.string.app_name)
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.heightOf(attr(android.R.attr.actionBarSize))
    )
  }

  private val notesList = themed(RecyclerView(context)).apply {
    layoutManager = LinearLayoutManager(context)
    adapter = noteAdapter
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  private val newNoteFab = themed(FloatingActionButton(context)).apply {
    setImageResource(R.drawable.ic_note_add_24dp)
    themeAware {
//      view.backgroundTintList = ColorStateList.valueOf(background)
//      view.setColorFilter(ColorUtils.blendARGB(background, Color.BLACK, 0.65f))
    }
    applyLayout(
        x = rightTo { parent.right() - 24.dip },
        y = bottomTo { parent.bottom() - 24.dip }
    )
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
    context.startActivity(EditorActivity.intent(context))
  }

  @AssistedInject.Factory
  interface Factory {
    fun withContext(context: Context): HomeView
  }
}