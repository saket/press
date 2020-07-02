package press.sync

import dagger.Module
import dagger.Provides
import me.saket.press.shared.sync.SharedSyncComponent
import me.saket.press.shared.sync.SyncPreferencesPresenter
import me.saket.press.shared.sync.git.GitHostIntegrationPresenter

@Module
object SyncModule {

  @Provides
  fun preferencesPresenter(): SyncPreferencesPresenter =
    SharedSyncComponent.preferencesPresenter()

  @Provides
  fun integrationPresenter(): GitHostIntegrationPresenter.Factory {
    // SAM conversion of Kotlin interfaces would have been nice.
    return object : GitHostIntegrationPresenter.Factory {
      override fun create(args: GitHostIntegrationPresenter.Args) =
        SharedSyncComponent.integrationPresenter(args)
    }
  }
}
