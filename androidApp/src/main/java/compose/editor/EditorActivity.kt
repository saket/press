package compose.editor

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import com.benasher44.uuid.uuid4
import com.google.android.material.floatingactionbutton.FloatingActionButton
import compose.ComposeApp
import compose.animation.FabTransform
import compose.theme.themeAware
import compose.widgets.PorterDuffColorFilterWrapper
import compose.widgets.ThemeAwareActivity
import compose.widgets.hideKeyboard
import compose.widgets.showKeyboard
import me.saket.compose.shared.editor.EditorOpenMode.NewNote
import me.saket.compose.shared.navigation.RealNavigator
import me.saket.compose.shared.navigation.ScreenKey.Back
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class EditorActivity : ThemeAwareActivity() {

  @field:Inject
  lateinit var editorViewFactory: EditorView.Factory
  private val editorView: EditorView by lazy(NONE) {
    val navigator = RealNavigator { screenKey ->
      when (screenKey) {
        Back -> dismiss()
        else -> error("Unhandled $screenKey")
      }
    }

    editorViewFactory.create(
        context = this@EditorActivity,
        openMode = NewNote(uuid4()),
        navigator = navigator
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    ComposeApp.component.inject(this)
    super.onCreate(savedInstanceState)

    setContentView(editorView)
    themeAware { palette ->
      editorView.setBackgroundColor(palette.window.backgroundColor)
    }

    editorView.editorEditText.showKeyboard()
    playEntryTransition()
  }

  private fun playEntryTransition() {
    if (FabTransform.hasActivityTransition(this)) {
      editorView.transitionName = SHARED_ELEMENT_TRANSITION_NAME
      FabTransform.setupActivityTransition(this, editorView)
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

  override fun onBackPressed() {
    dismiss()
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