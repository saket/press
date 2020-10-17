package press.widgets

import android.app.ActivityManager.TaskDescription
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.drawable.toBitmap
import me.saket.press.R
import me.saket.resourceinterceptor.ContextResourceWrapper
import me.saket.resourceinterceptor.InterceptibleResources
import press.theme.AutoThemer
import press.theme.TintedCursorDrawableInterceptor
import press.theme.themeAware

abstract class ThemeAwareActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    applyPaletteTheme()
    super.onCreate(savedInstanceState)
    AutoThemer.theme(this)
  }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(
        ContextResourceWrapper(newBase, InterceptibleResources(newBase.resources))
    )
  }

  @Suppress("DEPRECATION")
  private fun applyPaletteTheme() {
    // TODO: UGH. Find a better way to tint cursors or give up if it's not worth the effort!
    val resources = (baseContext as ContextThemeWrapper).baseContext.resources as InterceptibleResources

    // EditText cursor.
    resources.setInterceptor(
        resId = R.drawable.tinted_cursor_drawable,
        interceptor = TintedCursorDrawableInterceptor(this)
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
