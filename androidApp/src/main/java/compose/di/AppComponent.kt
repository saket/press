package compose.di

import compose.editor.EditorActivity
import compose.home.HomeActivity
import compose.home.HomeView
import dagger.Component
import io.reactivex.Observable
import me.saket.compose.shared.theme.ThemePalette

@Component(modules = [AppModule::class])
interface AppComponent {
  fun inject(target: HomeActivity)
  fun inject(target: HomeView)
  fun inject(target: EditorActivity)

  fun themePalette(): Observable<ThemePalette>
}