package press.sync

import dagger.Module
import dagger.Provides
import me.saket.press.shared.sync.SharedSyncComponent
import me.saket.press.shared.sync.git.GitHostIntegrationPresenter
import me.saket.press.shared.sync.git.NewGitRepositoryPresenter

@Module
object SyncModule {
  @Provides
  fun preferencesPresenter() =
    SharedSyncComponent.preferencesPresenter()

  @Provides
  fun integrationPresenter() =
    GitHostIntegrationPresenter.Factory { SharedSyncComponent.integrationPresenter(it) }

  @Provides
  fun newGitRepositoryPresenter() =
    NewGitRepositoryPresenter.Factory { SharedSyncComponent.newGitRepositoryPresenter(it) }

  @Provides
  fun statsPresenter() =
    SharedSyncComponent.statsForNerdsPresenter()
}
