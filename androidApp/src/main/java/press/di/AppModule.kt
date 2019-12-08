package press.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import press.editor.EditorModule
import press.home.HomeModule
import press.theme.ThemeModule
import dagger.Module
import dagger.Provides
import me.saket.press.shared.di.SharedAppComponent
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.localization.SharedLocalizationComponent

@AssistedModule
@Module(includes = [
  AssistedInject_AppModule::class,
  ThemeModule::class,
  HomeModule::class,
  EditorModule::class
])
object AppModule {
  @Provides
  fun strings(): Strings = SharedLocalizationComponent.strings()
}
