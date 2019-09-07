package compose.widgets

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import compose.util.onDestroys
import io.reactivex.Observable
import me.saket.compose.R
import me.saket.compose.shared.theme.ThemePalette
import me.saket.resourceinterceptor.ResourceInterceptibleContext
import javax.inject.Inject

abstract class ThemeAwareActivity : AppCompatActivity() {

  @field:Inject
  lateinit var palette: Observable<ThemePalette>

  override fun onCreate(savedInstanceState: Bundle?) {
    applyPaletteTheme()
    super.onCreate(savedInstanceState)
  }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(object : ResourceInterceptibleContext(newBase) {
      override fun interceptDrawable(resId: Int, theme: Resources.Theme?): Drawable? {
        return when (resId) {
          // TODO: tint cursor.
          R.drawable.tinted_cursor_drawable -> super.interceptDrawable(resId, theme)
          else -> super.interceptDrawable(resId, theme)
        }
      }
    })
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