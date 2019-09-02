package compose.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import compose.ComposeApp
import compose.theme.AppTheme
import compose.util.onDestroys
import io.reactivex.Observable
import javax.inject.Inject

class HomeActivity : AppCompatActivity() {

  @field:Inject
  lateinit var theme: Observable<AppTheme>

  @field:Inject
  lateinit var homeView: HomeView.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    ComposeApp.component.inject(this)
    theme.autoApply(this)
    super.onCreate(savedInstanceState)
    setContentView(homeView.withContext(this))
  }
}

private fun Observable<AppTheme>.autoApply(activity: AppCompatActivity) {
  takeUntil(activity.onDestroys()).subscribe { it.apply(activity) }
}