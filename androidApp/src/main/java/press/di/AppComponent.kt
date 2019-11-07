package press.di

import press.editor.EditorActivity
import press.home.HomeActivity
import press.home.HomeView
import dagger.Component
import io.reactivex.Observable
import me.saket.press.shared.theme.ThemePalette

@Component(modules = [AppModule::class])
interface AppComponent {
  fun inject(target: HomeActivity)
  fun inject(target: HomeView)
  fun inject(target: EditorActivity)

  fun themePalette(): Observable<ThemePalette>
}