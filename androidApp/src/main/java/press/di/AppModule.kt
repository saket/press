package press.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.Provides
import me.saket.press.shared.SharedAppComponent
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.sync.SharedSyncComponent
import me.saket.press.shared.sync.Syncer
import press.editor.EditorModule
import press.home.HomeModule
import press.sync.SyncModule
import press.theme.ThemeModule

@AssistedModule
@Module(includes = [
  AssistedInject_AppModule::class,
  ThemeModule::class,
  HomeModule::class,
  EditorModule::class,
  SyncModule::class
])
object AppModule {
  @Provides
  fun strings(): Strings = SharedAppComponent.strings()

  @Provides
  fun syncer(): Syncer = SharedSyncComponent.syncer()
}
