package press.editor

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_TEXT
import android.graphics.Color.BLACK
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.annotation.DrawableRes
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.StandaloneExpandablePageLayout
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import press.App
import press.animation.FabTransform
import press.util.withOpacity
import press.widgets.ThemeAwareActivity
import press.widgets.dp
import press.widgets.hideKeyboard
import press.widgets.interceptPullToCollapseOnView
import press.widgets.showKeyboard
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class EditorActivity : ThemeAwareActivity() {

  @field:Inject lateinit var editorViewFactory: EditorView.Factory
  private val editorView: EditorView by lazy(NONE) { createEditorView() }

  override fun onCreate(savedInstanceState: Bundle?) {
    App.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(wrapInExpandableLayout(editorView))

    val hasTransition = FabTransform.hasActivityTransition(this)
    if (hasTransition) {
      FabTransform.applyActivityTransition(this, editorView)
    }

    // The cursor doesn't show up when a shared element transition is used :/
    val delayFocus = if (hasTransition) FabTransform.ANIM_DURATION_MILLIS else 0
    Observable.timer(delayFocus, MILLISECONDS, mainThread())
        .takeUntil(editorView.detaches())
        .subscribe {
          if (intent.hasExtra(EXTRA_TEXT).not()) {
            editorView.editorEditText.showKeyboard()
          }
        }
  }

  override fun onBackPressed() {
    dismiss()
  }

  private fun createEditorView(): EditorView {
    val note = intent.getStringExtra(EXTRA_TEXT)

    return editorViewFactory.create(
        context = this@EditorActivity,
        openMode = NewNote(readNoteUuid(intent), note),
        onDismiss = ::dismiss
    )
  }

  private fun wrapInExpandableLayout(view: EditorView): ExpandablePageLayout {
    window.setBackgroundDrawable(ColorDrawable(BLACK.withOpacity(0.1f)))

    return StandaloneExpandablePageLayout(this).apply {
      elevation = dp(40f)
      onPageRelease = { collapseEligible ->
        if (collapseEligible) {
          dismiss()
        }
      }
      pullToCollapseInterceptor = interceptPullToCollapseOnView(view.scrollView)
      expandImmediately()
      addView(view)
    }
  }

  private fun dismiss() {
    editorView.hideKeyboard()
    if (FabTransform.hasActivityTransition(this)) {
      finishAfterTransition()
    } else {
      super.finish()
    }
  }

  companion object {
    private const val KEY_NOTE_ID = "press:new_note_id"

    private fun readNoteUuid(intent: Intent): Uuid {
      return uuidFrom(intent.getStringExtra(KEY_NOTE_ID)!!)
    }

    fun intent(context: Context, preFilledNote: String? = null): Intent {
      return Intent(context, EditorActivity::class.java).apply {
        putExtra(KEY_NOTE_ID, uuid4().toString())
        if (preFilledNote != null) {
          putExtra(EXTRA_TEXT, preFilledNote)
        }
      }
    }

    @JvmStatic
    fun intentWithFabTransform(
      activity: Activity,
      fab: FloatingActionButton,
      @DrawableRes fabIconRes: Int
    ): Pair<Intent, ActivityOptions> {
      val intent = intent(activity)
      val options = FabTransform.createOptions(activity, intent, fab, fabIconRes)
      return intent to options
    }
  }
}
