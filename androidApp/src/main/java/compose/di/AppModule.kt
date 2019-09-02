package compose.di

import compose.home.HomeModule
import compose.theme.ThemeModule
import dagger.Module

@Module(includes = [
  HomeModule::class,
  ThemeModule::class
])
object AppModule