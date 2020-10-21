package press.di

import press.editor.EditorActivity
import press.home.HomeActivity
import dagger.Component
import io.reactivex.Observable
import me.saket.press.shared.theme.ThemePalette
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.sync.SyncCoordinator
import me.saket.press.shared.theme.AppTheme
import press.PressApp
import press.sync.GitHostIntegrationActivity
import press.sync.PreferencesActivity
import press.sync.stats.SyncStatsForNerdsActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
  fun strings(): Strings
  fun theme(): AppTheme
  fun syncCoordinator(): SyncCoordinator

  fun inject(target: PressApp)
  fun inject(target: HomeActivity)
  fun inject(target: EditorActivity)
  fun inject(target: PreferencesActivity)
  fun inject(target: GitHostIntegrationActivity)
  fun inject(target: SyncStatsForNerdsActivity)
}
