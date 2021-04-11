package press.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import me.saket.press.shared.syncer.SyncCoordinator
import me.saket.press.shared.theme.AppTheme
import me.saket.press.shared.ui.ScreenResults
import press.navigation.ViewFactories
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
  fun theme(): AppTheme
  fun syncCoordinator(): SyncCoordinator
  fun viewFactories(): ViewFactories
  fun screenResults(): ScreenResults

  @Component.Builder
  interface Builder {
    @BindsInstance
    fun application(app: Application): Builder
    fun build(): AppComponent
  }
}
