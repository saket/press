package press.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import press.editor.EditorModule
import press.home.HomeModule
import press.theme.ThemeModule
import dagger.Module

@AssistedModule
@Module(includes = [
  AssistedInject_AppModule::class,
  ThemeModule::class,
  HomeModule::class,
  EditorModule::class
])
object AppModule