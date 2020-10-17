package press.widgets

import android.app.ActivityManager.TaskDescription
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.drawable.toBitmap
import me.saket.press.R
import press.theme.AutoThemer
import press.theme.themeAware

abstract class ThemeAwareActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    applyPaletteTheme()
    super.onCreate(savedInstanceState)
    AutoThemer.theme(this)
  }

  @Suppress("DEPRECATION")
  private fun applyPaletteTheme() {
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
