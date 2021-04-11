package press.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import com.squareup.inject.inflation.InflationModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import me.saket.press.shared.SharedAppComponent
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.preferences.UserPreferences
import me.saket.press.shared.syncer.SyncCoordinator
import me.saket.press.shared.theme.AppTheme
import me.saket.press.shared.ui.ScreenResults
import press.editor.EditorModule
import press.home.HomeModule
import press.preferences.PreferencesModule
import press.theme.AndroidAppTheme

@InflationModule
@AssistedModule
@Module(
  includes = [
    InflationInject_AppModule::class,
    AssistedInject_AppModule::class,
    HomeModule::class,
    EditorModule::class,
    PreferencesModule::class
  ]
)
abstract class AppModule {
  companion object {
    @Provides
    fun strings(): Strings = SharedAppComponent.strings()

    @Provides
    fun syncCoordinator(): SyncCoordinator = SharedAppComponent.syncCoordinator()

    @Provides
    fun screenResults(): ScreenResults = SharedAppComponent.screenResults()

    @Provides
    fun userPreferences(): UserPreferences = SharedAppComponent.userPreferences()
  }

  @Binds
  abstract fun appTheme(android: AndroidAppTheme): AppTheme
}
