package compose.widgets

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import compose.ComposeApp
import compose.theme.ThemePalette
import compose.util.onDestroys
import io.reactivex.Observable
import javax.inject.Inject

abstract class ThemeAwareActivity : AppCompatActivity() {

  @field:Inject
  lateinit var palette: Observable<ThemePalette>

  override fun onCreate(savedInstanceState: Bundle?) {
    ComposeApp.component.inject(this)
    palette.autoApply(this)
    super.onCreate(savedInstanceState)
  }

  private fun Observable<ThemePalette>.autoApply(activity: AppCompatActivity) {
    takeUntil(activity.onDestroys()).subscribe { theme ->
      activity.window.apply {
        setBackgroundDrawable(ColorDrawable(theme.windowTheme.backgroundColor))
        addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor = theme.primaryColorDark
      }
    }
  }
}