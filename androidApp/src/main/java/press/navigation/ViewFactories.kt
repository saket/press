package press.navigation

import android.content.Context
import android.view.View
import com.squareup.inject.inflation.ViewFactory
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.preferences.PreferenceCategory.AboutApp
import me.saket.press.shared.preferences.PreferenceCategory.Editor
import me.saket.press.shared.preferences.PreferenceCategory.LookAndFeel
import me.saket.press.shared.preferences.PreferenceCategory.Sync
import me.saket.press.shared.preferences.PreferenceCategoryScreenKey
import me.saket.press.shared.preferences.PreferencesScreenKey
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationScreenKey
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryScreenKey
import me.saket.press.shared.preferences.sync.stats.SyncStatsForNerdsScreenKey
import me.saket.press.shared.ui.ScreenKey
import press.editor.EditorView
import press.home.HomeView
import press.preferences.PreferencesView
import press.preferences.about.AboutAppPreferencesView
import press.preferences.editor.EditorPreferencesView
import press.preferences.lookandfeel.LookAndFeelPreferencesView
import press.preferences.sync.SyncPreferencesView
import press.preferences.sync.setup.GitHostIntegrationView
import press.preferences.sync.setup.NewGitRepositoryView
import press.preferences.sync.stats.SyncStatsForNerdsView
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Inflation Inject exposes view factories using dagger multi-bindings which means
 * it can be used for injecting views even when not _inflating_ them.
 */
class ViewFactories @Inject constructor(
  private val factories: Map<String, @JvmSuppressWildcards ViewFactory>
) {

  @Suppress("UNCHECKED_CAST")
  fun createView(context: Context, screen: ScreenKey): View {
    // These screen keys are defined in a shared multiplatform module, which
    // is unaware of Android Views so they must be manually mapped here.
    return when (screen) {
      is PreferencesScreenKey -> PreferencesView(context)
      is PreferenceCategoryScreenKey -> {
        when (screen.category) {
          LookAndFeel -> LookAndFeelPreferencesView(context)
          Editor -> EditorPreferencesView(context)
          Sync -> generate(context, SyncPreferencesView::class)
          AboutApp -> AboutAppPreferencesView(context)
        }
      }
      else -> {
        val viewClass = when (screen) {
          is HomeScreenKey -> HomeView::class
          is EditorScreenKey -> EditorView::class
          is GitHostIntegrationScreenKey -> GitHostIntegrationView::class
          is NewGitRepositoryScreenKey -> NewGitRepositoryView::class
          is SyncStatsForNerdsScreenKey -> SyncStatsForNerdsView::class
          else -> error("Missing mapping for ${screen::class.simpleName}")
        }
        generate(context, viewClass)
      }
    }
  }

  private fun generate(context: Context, viewClass: KClass<*>): View {
    val factoryKey = viewClass.qualifiedName
    val factory = factories[factoryKey] ?: error("No ViewFactory found for $factoryKey among: ${factories.keys}")
    return factory.create(context, null /* attrs */)
  }
}
