package press.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.ui.Navigator
import press.PressApp
import press.extensions.hideKeyboard
import press.extensions.unsafeLazy
import press.widgets.ThemeAwareActivity

class TheActivity : ThemeAwareActivity(), HasNavigator {
  override lateinit var navigator: Navigator
  private val navHostView by unsafeLazy { FrameLayout(this) }

  override fun attachBaseContext(newBase: Context) {
    val screenChanger = ScreenKeyChanger(
      hostView = { navHostView },
      viewFactories = PressApp.component.viewFactories(),
      transitions = listOf(
        ExpandableScreenTransition(),
        MorphFromFabScreenTransition()
      )
    )
    screenChanger.focusChangeListeners += HideKeyboardOnScreenChange()
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

    val initialScreen = if (intent.action == Intent.ACTION_SEND) {
      EditorScreenKey(
        openMode = NewNote(
          placeholderId = NoteId.generate(),
          preFilledNote = intent.getStringExtra(Intent.EXTRA_TEXT)
        ),
        showKeyboard = true
      )
    } else {
      HomeScreenKey()
    }

    navigator.clearTopAndLfg(initialScreen)
  }

  override fun onBackPressed() {
    if (!navigator.goBack()) {
      super.onBackPressed()
    }
  }
}

private class HideKeyboardOnScreenChange : ScreenFocusChangeListener {
  private var lastFocusedScreen: View? = null

  override fun onScreenFocusChanged(focusedScreen: View?) {
    val isScreenChanging = focusedScreen != null && lastFocusedScreen != focusedScreen
    if (isScreenChanging && focusedScreen!!.findFocus() !is EditText) {
      focusedScreen!!.hideKeyboard()
    }
    lastFocusedScreen = focusedScreen
  }
}
