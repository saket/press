package compose.di

import compose.editor.EditorModule
import compose.home.HomeModule
import compose.theme.ThemeModule
import dagger.Module

@Module(includes = [
  ThemeModule::class,
  HomeModule::class,
  EditorModule::class
])
object AppModule