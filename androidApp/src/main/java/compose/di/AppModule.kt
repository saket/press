package compose.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import compose.editor.EditorModule
import compose.home.HomeModule
import compose.theme.ThemeModule
import dagger.Module

@AssistedModule
@Module(includes = [
  AssistedInject_AppModule::class,
  ThemeModule::class,
  HomeModule::class,
  EditorModule::class
])
object AppModule