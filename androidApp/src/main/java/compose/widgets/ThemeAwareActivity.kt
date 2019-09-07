package compose.widgets

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import compose.util.onDestroys
import io.reactivex.Observable
import me.saket.compose.shared.theme.ThemePalette
import javax.inject.Inject

abstract class ThemeAwareActivity : AppCompatActivity() {

  @field:Inject
  lateinit var palette: Observable<ThemePalette>

  override fun onCreate(savedInstanceState: Bundle?) {
    applyPaletteTheme()
    super.onCreate(savedInstanceState)
  }

  private fun applyPaletteTheme() {
    palette
        .takeUntil(onDestroys())
        .subscribe { theme ->
          window.apply {
            setBackgroundDrawable(ColorDrawable(theme.windowTheme.backgroundColor))
            addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = theme.primaryColorDark
          }
        }
  }
}