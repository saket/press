package compose.widgets

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import compose.util.onDestroys
import io.reactivex.Observable
import me.saket.compose.shared.theme.ThemePalette
import me.saket.resourceinterceptor.ContextResourceWrapper
import me.saket.resourceinterceptor.InterceptibleResources
import javax.inject.Inject

abstract class ThemeAwareActivity : AppCompatActivity() {

  @field:Inject
  lateinit var palette: Observable<ThemePalette>

  override fun onCreate(savedInstanceState: Bundle?) {
    applyPaletteTheme()
    super.onCreate(savedInstanceState)
  }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(
        ContextResourceWrapper(newBase, InterceptibleResources(newBase.resources))
    )
  }

  private fun applyPaletteTheme() {
    palette
        .takeUntil(onDestroys())
        .subscribe { palette ->
          window.apply {
            setBackgroundDrawable(ColorDrawable(palette.windowTheme.backgroundColor))
            addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = palette.primaryColorDark
          }
        }
  }
}
