package press.editor

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import com.benasher44.uuid.uuid4
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import press.App
import press.animation.FabTransform
import press.theme.themeAware
import press.widgets.PorterDuffColorFilterWrapper
import press.widgets.ThemeAwareActivity
import press.widgets.hideKeyboard
import press.widgets.showKeyboard
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.navigation.RealNavigator
import me.saket.press.shared.navigation.ScreenKey.Back
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class EditorActivity : ThemeAwareActivity() {

  @field:Inject lateinit var editorViewFactory: EditorView.Factory
  private val editorView: EditorView by lazy(NONE) { createEditorView() }

  override fun onCreate(savedInstanceState: Bundle?) {
    App.component.inject(this)
    super.onCreate(savedInstanceState)

    setContentView(editorView)
    themeAware { palette ->
      editorView.setBackgroundColor(palette.window.backgroundColor)
    }

    val hasTransition = FabTransform.hasActivityTransition(this)
    if (hasTransition) {
      FabTransform.applyActivityTransition(this, editorView)
    }

    // The cursor doesn't show up when a shared element transition is used :/
    val delayFocus = if (hasTransition) FabTransform.ANIM_DURATION_MILLIS else 0
    Observable.timer(delayFocus, MILLISECONDS, mainThread())
        .takeUntil(editorView.detaches())
        .subscribe {
          editorView.editorEditText.showKeyboard()
        }
  }

  override fun onBackPressed() {
    dismiss()
  }

  private fun createEditorView(): EditorView {
    val navigator = RealNavigator { screenKey ->
      when (screenKey) {
        Back -> dismiss()
        else -> error("Unhandled $screenKey")
      }
    }

    return editorViewFactory.create(
        context = this@EditorActivity,
        openMode = NewNote(uuid4()),
        navigator = navigator
    )
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
    private const val SHARED_ELEMENT_TRANSITION_NAME = "sharedElement:EditorActivity"

    private fun intent(context: Context): Intent = Intent(context, EditorActivity::class.java)

    @JvmStatic
    fun intentWithFabTransform(
      activity: Activity,
      fab: FloatingActionButton,
      @DrawableRes fabIconRes: Int
    ): Pair<Intent, ActivityOptions> {
      fab.transitionName = SHARED_ELEMENT_TRANSITION_NAME

      val intent = intent(activity)

      val fabColor = fab.backgroundTintList!!.defaultColor
      val fabIconTint = (fab.colorFilter as PorterDuffColorFilterWrapper).color

      FabTransform.addExtras(intent, fabColor, fabIconRes, fabIconTint)
      val options = ActivityOptions.makeSceneTransitionAnimation(
          activity,
          fab,
          SHARED_ELEMENT_TRANSITION_NAME
      )
      return intent to options
    }
  }
}
