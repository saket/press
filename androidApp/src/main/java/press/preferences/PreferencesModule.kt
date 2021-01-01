package press.preferences

import dagger.Module
import dagger.Provides
import me.saket.press.shared.preferences.SharedPreferencesComponent
import me.saket.press.shared.preferences.sync.SyncPreferencesPresenter
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationPresenter
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryPresenter

@Module
object PreferencesModule {
  @Provides
  fun preferencesPresenter() =
    SyncPreferencesPresenter.Factory { SharedPreferencesComponent.preferencesPresenter(it) }

  @Provides
  fun integrationPresenter() =
    GitHostIntegrationPresenter.Factory { SharedPreferencesComponent.integrationPresenter(it) }

  @Provides
  fun newGitRepositoryPresenter() =
    NewGitRepositoryPresenter.Factory { SharedPreferencesComponent.newGitRepositoryPresenter(it) }

  @Provides
  fun statsPresenter() =
    SharedPreferencesComponent.statsForNerdsPresenter()
}
