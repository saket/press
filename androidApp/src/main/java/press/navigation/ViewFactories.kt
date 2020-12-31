package press.navigation

import android.content.Context
import android.view.View
import com.squareup.inject.inflation.ViewFactory
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.preferences.PreferencesScreenKey
import me.saket.press.shared.sync.SyncPreferencesScreenKey
import me.saket.press.shared.sync.git.GitHostIntegrationScreenKey
import me.saket.press.shared.sync.git.NewGitRepositoryScreenKey
import me.saket.press.shared.sync.stats.SyncStatsForNerdsScreenKey
import me.saket.press.shared.ui.ScreenKey
import press.editor.EditorView
import press.home.HomeView
import press.preferences.PreferencesView
import press.sync.GitHostIntegrationView
import press.sync.NewGitRepositoryView
import press.sync.SyncPreferencesView
import press.sync.stats.SyncStatsForNerdsView
import javax.inject.Inject

/**
 * Inflation Inject exposes view factories using dagger multi-bindings which means
 * it can be used for injecting views even when not _inflating_ them.
 */
class ViewFactories @Inject constructor(
  private val factories: Map<String, @JvmSuppressWildcards ViewFactory>
) {

  @Suppress("UNCHECKED_CAST")
  fun createView(context: Context, screen: ScreenKey): View {
    // The screen keys are defined in a shared multiplatform module, which
    // is unaware of Android Views so they must be manually mapped here.
    val name = when (screen) {
      is HomeScreenKey -> HomeView::class
      is EditorScreenKey -> EditorView::class
      is PreferencesScreenKey -> PreferencesView::class
      is SyncPreferencesScreenKey -> SyncPreferencesView::class
      is GitHostIntegrationScreenKey -> GitHostIntegrationView::class
      is NewGitRepositoryScreenKey -> NewGitRepositoryView::class
      is SyncStatsForNerdsScreenKey -> SyncStatsForNerdsView::class
      else -> error("Missing mapping for ${screen::class.simpleName}")
    }.qualifiedName

    val factory = factories[name] ?: error("No ViewFactory found for $name. Have factories for: ${factories.keys}")
    return factory.create(context, null)
  }
}
