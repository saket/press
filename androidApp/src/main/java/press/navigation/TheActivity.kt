package press.navigation

import android.content.Context
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.ScreenKey
import press.PressApp
import press.extensions.hideKeyboard
import press.extensions.unsafeLazy
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Ignored
import press.navigation.transitions.ExpandableScreenTransition
import press.navigation.transitions.MorphFromFabScreenTransition
import press.widgets.ThemeAwareActivity

class TheActivity : ThemeAwareActivity(), HasNavigator {
  override lateinit var navigator: Navigator
  private val navHostView by unsafeLazy { FrameLayout(this) }

  override fun attachBaseContext(newBase: Context) {
    val screenChanger = ScreenKeyChanger(
      hostView = { navHostView },
      viewFactories = PressApp.component.viewFactories(),
      transitions = listOf(
        HideKeyboardOnScreenChange(),
        ExpandableScreenTransition(),
        MorphFromFabScreenTransition()
      )
    )
    navigator = RealNavigator(this, screenChanger).also {
      super.attachBaseContext(it.installInContext(newBase, PlaceholderScreenKey()))
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(navHostView)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    if (savedInstanceState == null) {
      val initialScreen = if (intent.action == ACTION_SEND) {
        val sharedText = buildString {
          intent.getStringExtra(EXTRA_SUBJECT)?.let { append("# $it\n") }
          intent.getStringExtra(EXTRA_TEXT)?.let(::append)
        }
        EditorScreenKey(
          openMode = NewNote(placeholderId = NoteId.generate(), preFilledNote = sharedText),
          showKeyboard = true
        )
      } else {
        HomeScreenKey
      }
      navigator.clearTopAndLfg(initialScreen)
    }
  }

  override fun onBackPressed() {
    if (!navigator.goBack()) {
      super.onBackPressed()
    }
  }
}

private class HideKeyboardOnScreenChange : ScreenTransition {
  override fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    goingForward: Boolean,
    onComplete: () -> Unit
  ): TransitionResult {
    if (toView.findFocus() !is EditText) {
      toView.hideKeyboard()
    }
    return Ignored
  }
}
