package press.theme

import dagger.Module
import dagger.Provides
import me.saket.press.shared.theme.AppTheme
import me.saket.press.shared.theme.palettes.DraculaThemePalette
import javax.inject.Singleton

@Module
object ThemeModule {
  @Provides
  @Singleton
  fun theme(): AppTheme = AppTheme(default = DraculaThemePalette)
}
