package press.home

import android.app.Activity
import android.content.Context
import android.graphics.Color.BLACK
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.animation.PathInterpolator
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
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
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.parcel.Parcelize
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.dimming.TintPainter
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.InterceptResult.IGNORED
import me.saket.inboxrecyclerview.page.InterceptResult.INTERCEPTED
import me.saket.inboxrecyclerview.page.OnPullToCollapseInterceptor
import me.saket.press.R
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.home.HomeEvent
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.home.HomeEvent.WindowFocusChanged
import me.saket.press.shared.home.HomePresenter
import me.saket.press.shared.home.HomePresenter.Args
import me.saket.press.shared.home.HomeUiEffect
import me.saket.press.shared.home.HomeUiEffect.ComposeNewNote
import me.saket.press.shared.home.HomeUiModel
import me.saket.press.shared.subscribe
import me.saket.press.shared.uiUpdates
import press.editor.EditorActivity
import press.editor.EditorView
import press.theme.themeAware
import press.theme.themed
import press.util.exhaustive
import press.util.heightOf
import press.util.suspendWhile
import press.util.throttleFirst
import press.widgets.BackPressInterceptResult
import press.widgets.BackPressInterceptResult.BACK_PRESS_IGNORED
import press.widgets.BackPressInterceptResult.BACK_PRESS_INTERCEPTED
import press.widgets.SpacingBetweenItemsDecoration
import press.widgets.addStateChangeCallbacks
import press.widgets.attr
import press.widgets.doOnNextCollapse
import press.widgets.locationOnScreen
import press.widgets.suspendWhileExpanded
import kotlin.properties.Delegates.observable

class HomeView @AssistedInject constructor(
  @Assisted context: Context,
  private val noteAdapter: NoteAdapter,
  private val presenter: HomePresenter.Factory,
  private val editorViewFactory: EditorView.Factory
) : ContourLayout(context) {

  companion object {
    private const val KEY_SUPER = "press::key::super"
    private const val KEY_NOTE_MODEL = "press::key::note"
  }

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
    layoutManager = LinearLayoutManager(context)
    adapter = noteAdapter
    tintPainter = TintPainter.uncoveredArea(color = BLACK, opacity = 0.25f)
    itemAnimator = AlphaInAnimator()
    toolbar.doOnLayout {
      updatePadding(top = toolbar.height)
    }
    addItemDecoration(SpacingBetweenItemsDecoration(1.dip))
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.bottomTo { parent.bottom() }
    )
  }

  private val newNoteFab = themed(FloatingActionButton(context)).apply {
    setImageResource(R.drawable.ic_note_add_24dp)
    applyLayout(
        x = rightTo { parent.right() - 24.dip },
        y = bottomTo { parent.bottom() - 24.dip }
    )
  }

  // Clarify the type here to clear the 'type checking recursive problem'.
  private val noteEditorPage: ExpandablePageLayout = ExpandablePageLayout(context).apply {
    doOnNextCollapse { noteForEditor = null }
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

  private val createEditorView = { noteUuid: Uuid ->
    editorViewFactory.create(
        context = context,
        openMode = ExistingNote(noteUuid),
        onDismiss = { notesList.collapse() }
    )
  }

  private var noteForEditor by observable<NoteParcelable?>(null) { _, oldNote, newNote ->
    if (oldNote == newNote) return@observable
    if (newNote != null) {
      val editorView = createEditorView(requireNotNull(Uuid.parse(newNote.noteUuid)))
      editorView.id = R.id.editorViewId
      noteEditorPage.addView(editorView)
      noteEditorPage.doOnNextCollapse { it.removeView(editorView) }

      val keyboardToggle = HideKeyboardOnPageCollapse(editorView.editorEditText)
      noteEditorPage.addStateChangeCallbacks(keyboardToggle)
      noteEditorPage.doOnNextCollapse { it.removeStateChangeCallbacks(keyboardToggle) }

      noteEditorPage.pullToCollapseInterceptor =
        interceptIfViewCanBeScrolled(editorView.scrollView)

      noteEditorPage.post {
        notesList.expandItem(itemId = newNote.adapterId, immediate = false)
      }
    } else {
      notesList.collapse()
    }
  }

  init {
    setupNoteEditorPage()
  }

  override fun onSaveInstanceState(): Parcelable? {
    val bundle = Bundle()
    val superState = super.onSaveInstanceState()
    if (superState != null) bundle.putParcelable(KEY_SUPER, superState)
    noteForEditor?.let { bundle.putParcelable(KEY_NOTE_MODEL, it) }
    return bundle
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    var savedState = state
    if (savedState is Bundle) {
      this.noteForEditor = savedState.getParcelable(KEY_NOTE_MODEL)
      savedState = savedState.getParcelable(KEY_SUPER)
    }
    super.onRestoreInstanceState(savedState)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val newNoteClicks = newNoteFab
        .clicks()
        .map<HomeEvent> { NewNoteClicked }

    val presenter = presenter.create(Args(includeEmptyNotes = false))

    newNoteClicks.uiUpdates(presenter)
        .suspendWhileExpanded(noteEditorPage)
        .suspendWhile(windowFocusChanges) { it.hasFocus.not() }
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(models = ::render, effects = ::render)
  }

  override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
    super.onWindowFocusChanged(hasWindowFocus)
    windowFocusChanges.onNext(WindowFocusChanged(hasWindowFocus))
  }

  private fun setupNoteEditorPage() {
    noteAdapter.noteClicks
        .throttleFirst(1.seconds, mainThread())
        .takeUntil(detaches())
        .subscribe { noteModel ->
          this.noteForEditor = NoteParcelable(
              noteModel.noteUuid.toString(),
              noteModel.adapterId
          )
        }

    noteEditorPage.addStateChangeCallbacks(
        ToggleFabOnPageStateChange(newNoteFab),
        ToggleSoftInputModeOnPageStateChange(activity.window)
    )
  }

  private fun interceptIfViewCanBeScrolled(view: View): OnPullToCollapseInterceptor {
    return { downX, downY, upwardPull ->
      val touchLiesOnView = view.locationOnScreen()
          .contains(downX.toInt(), downY.toInt())

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

  private fun render(effect: HomeUiEffect) {
    when (effect) {
      ComposeNewNote -> openNewNoteScreen()
    }.exhaustive
  }

  private fun openNewNoteScreen() {
    val (intent, options) = EditorActivity.intentWithFabTransform(
        activity = activity,
        fab = newNoteFab,
        fabIconRes = R.drawable.ic_note_add_24dp
    )
    ContextCompat.startActivity(context, intent, options.toBundle())
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

@Parcelize
class NoteParcelable(
  val noteUuid: String, // Plan to use Uuid, but String is easier to parcelize.
  val adapterId: Long
) : Parcelable
