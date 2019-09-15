package compose.widgets

import android.app.ActivityManager.TaskDescription
import android.content.Context
import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.drawable.toBitmap
import compose.theme.themeAware
import me.saket.compose.R
import me.saket.resourceinterceptor.ContextResourceWrapper
import me.saket.resourceinterceptor.DrawableInterceptor
import me.saket.resourceinterceptor.InterceptibleResources

abstract class ThemeAwareActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    applyPaletteTheme()
    super.onCreate(savedInstanceState)
  }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(
        ContextResourceWrapper(newBase, InterceptibleResources(newBase.resources))
    )
  }

  @Suppress("DEPRECATION")
  private fun applyPaletteTheme() {
    val resources = resources as InterceptibleResources

    // TODO: This doesn't get updated as the drawable is only read once.
    //  Moving the listener to inside the Drawable might work, but taking
    //  care of leaks can be tricky.
    resources.setInterceptor(
        R.drawable.tinted_cursor_drawable,
        DrawableInterceptor { systemDrawable ->
          systemDrawable().apply {
            themeAware { palette ->
              setColorFilter(palette.accentColor, SRC_ATOP)
            }
          }
        }
    )

    themeAware { palette ->
      // Window chrome.
      window.apply {
        setBackgroundDrawable(ColorDrawable(palette.window.backgroundColor))
        addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor = palette.primaryColorDark
      }

      // For recent apps.
      val taskDescription = if (SDK_INT >= 28) {
        TaskDescription(getString(R.string.app_name), R.mipmap.ic_launcher, palette.primaryColor)
      } else {
        val appIcon = getDrawable(this, R.mipmap.ic_launcher)!!.toBitmap()
        TaskDescription(getString(R.string.app_name), appIcon, palette.primaryColor)
      }
      setTaskDescription(taskDescription)
    }
  }
}
