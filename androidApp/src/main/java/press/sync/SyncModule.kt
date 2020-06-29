package press.sync

import dagger.Module
import dagger.Provides
import me.saket.press.shared.sync.git.GitHostAuthPresenter
import me.saket.press.shared.sync.SharedSyncComponent

@Module
object SyncModule {

  @Provides
  fun authPresenter(): GitHostAuthPresenter = SharedSyncComponent.gitHostAuthPresenter()
}
