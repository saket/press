package press.di

import press.editor.EditorActivity
import press.home.HomeActivity
import press.home.HomeView
import dagger.Component
import io.reactivex.Observable
import me.saket.press.shared.theme.ThemePalette
import me.saket.press.shared.localization.Strings
import press.sync.SyncPreferencesActivity

@Component(modules = [AppModule::class])
interface AppComponent {
  fun themePalette(): Observable<ThemePalette>
  fun strings(): Strings

  fun inject(target: HomeActivity)
  fun inject(target: HomeView)
  fun inject(target: EditorActivity)
  fun inject(target: SyncPreferencesActivity)
}
