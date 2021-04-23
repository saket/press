package press.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.widget.EditText
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.subjects.PublishSubject
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.editor.PlaceholderNoteId
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationScreenKey
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

open class TheActivity : ThemeAwareActivity(), HasNavigator {
  override lateinit var navigator: Navigator
  private val navHostView by unsafeLazy { NavigationHostLayout(this) }

  companion object {
    private const val KEY_INITIAL_SCREEN = "initial_screen"
    val viewRecreateRequests = PublishSubject.create<Unit>()

    fun intent(
      context: Context,
      initialScreen: ScreenKey? = null,
      newTask: Boolean = false
    ): Intent {
      val targetActivity = if (newTask) NewTaskTheActivity::class.java else TheActivity::class.java
      return Intent(context, targetActivity).apply {
        if (initialScreen != null) {
          putExtra(KEY_INITIAL_SCREEN, initialScreen)
        }
      }
    }
  }

  override fun attachBaseContext(newBase: Context) {
    val screenChanger = ScreenKeyChanger(
      hostView = { navHostView },
      formFactor = PhoneFormFactor(PressApp.component.viewFactories()),
      transitions = listOf(
        HideKeyboardOnScreenChange(),
        MorphFromFabScreenTransition(),
        ExpandableScreenTransition(),
      )
    )
    val screenResults = PressApp.component.screenResults()
    navigator = RealNavigator(this, screenChanger, screenResults).also {
      super.attachBaseContext(it.installInContext(newBase, PlaceholderScreenKey()))
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(navHostView)

    viewRecreateRequests.takeUntil(navHostView.detaches()).subscribe {
      val savedState = SparseArray<Parcelable>()
      navHostView.saveHierarchyState(savedState)
      navHostView.removeAllViews()

      navigator.recreateScreens()
      navHostView.restoreHierarchyState(savedState)
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    if (savedInstanceState == null) {
      navigator.clearTopAndLfg(HomeScreenKey.root())
      readDeepLinkedScreen(intent)?.let(navigator::lfg)
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    readDeepLinkedScreen(intent)?.let(navigator::lfg)
  }

  private fun readDeepLinkedScreen(intent: Intent): ScreenKey? {
    return if (intent.hasExtra(KEY_INITIAL_SCREEN)) {
      intent.getParcelableExtra(KEY_INITIAL_SCREEN)!!

    } else if (intent.action == ACTION_SEND) {
      EditorScreenKey(
        NewNote(
          noteId = PlaceholderNoteId(NoteId.generate()),
          preFilledNote = buildString {
            intent.getStringExtra(EXTRA_SUBJECT)?.let { append("# $it\n") }
            intent.getStringExtra(EXTRA_TEXT)?.let(::append)
          }
        )
      )

    } else if (intent.action == ACTION_VIEW && intent.dataString?.startsWith("intent://press") == true) {
      GitHostIntegrationScreenKey(deepLink = intent.dataString!!)
    } else {
      null
    }
  }

  override fun onBackPressed() {
    navigator.goBack()
  }
}

private class HideKeyboardOnScreenChange : ScreenTransition {
  override fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    newBackground: View?,
    goingForward: Boolean,
    onComplete: () -> Unit
  ): TransitionResult {
    // Retain the keyboard only if the incoming screen needs it to.
    @SuppressLint("WrongConstant")
    if (toView.focusSearch(View.FOCUS_FORWARD) !is EditText) {
      toView.hideKeyboard()
    }
    return Ignored
  }
}
