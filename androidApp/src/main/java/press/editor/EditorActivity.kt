package press.editor

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_TEXT
import android.graphics.Color.BLACK
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.DrawableRes
import com.benasher44.uuid.uuidFrom
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.StandaloneExpandablePageLayout
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorOpenMode
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.ScreenKey
import press.PressApp
import press.animation.FabTransform
import press.extensions.hideKeyboard
import press.extensions.showKeyboard
import press.extensions.unsafeLazy
import press.extensions.withOpacity
import press.navigation.HasNavigator
import press.navigation.ViewFactories
import press.widgets.ThemeAwareActivity
import press.widgets.dp
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

// TODO(saket): Merge with HomeActivity.
class EditorActivity : ThemeAwareActivity(), HasNavigator {
  @Inject lateinit var viewFactories: ViewFactories
  private val openMode: EditorOpenMode by unsafeLazy { readOpenMode(intent) }
  private val editorView by unsafeLazy { viewFactories.createView(this, EditorScreenKey(openMode)) }

  override val navigator = object : Navigator {
    override fun lfg(screen: ScreenKey): Unit = error("Unsupported")
    override fun goBack() = true.also { finish() }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    PressApp.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(wrapInExpandableLayout(editorView))

    val hasTransition = FabTransform.hasActivityTransition(this)
    if (hasTransition) {
      FabTransform.applyActivityTransition(this, editorView)
    }

    if (openMode.showKeyboardOnStart()) {
      // The cursor doesn't show up when a shared element transition is used :/
      val delayFocus = if (hasTransition) FabTransform.ANIM_DURATION_MILLIS else 0
      Observable.timer(delayFocus, MILLISECONDS, mainThread())
        .takeUntil(editorView.detaches())
        .subscribe {
          (editorView.findFocus() as EditText).showKeyboard()
        }
    }
  }

  override fun onBackPressed() {
    dismiss()
  }

  private fun wrapInExpandableLayout(view: View): ExpandablePageLayout {
    window.setBackgroundDrawable(ColorDrawable(BLACK.withOpacity(0.1f)))

    return StandaloneExpandablePageLayout(this).apply {
      elevation = dp(40f)
      onPageRelease = { collapseEligible ->
        if (collapseEligible) {
          dismiss()
        }
      }
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
    private const val EXTRA_OPEN_MODE = "press:open_mode"

    private fun readOpenMode(intent: Intent): EditorOpenMode {
      return intent.getParcelableExtra(EXTRA_OPEN_MODE)!!
    }

    fun intent(context: Context, openMode: EditorOpenMode): Intent {
      return Intent(context, EditorActivity::class.java).putExtra(EXTRA_OPEN_MODE, openMode)
    }

    @JvmStatic
    fun intentWithFabTransform(
      activity: Activity,
      openMode: EditorOpenMode,
      fab: FloatingActionButton,
      @DrawableRes fabIconRes: Int
    ): Pair<Intent, ActivityOptions> {
      val intent = intent(activity, openMode)
      val options = FabTransform.createOptions(activity, intent, fab, fabIconRes)
      return intent to options
    }
  }
}
