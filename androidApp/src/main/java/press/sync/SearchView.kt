package press.sync

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.widget.EditText
import androidx.core.view.updatePadding
import com.google.android.material.textfield.TextInputLayout
import me.saket.press.shared.theme.blendWith
import press.extensions.textColor
import press.theme.themeAware
import press.theme.themed
import press.widgets.dp

class SearchView(context: Context) : TextInputLayout(context) {
  init {
    addView(themed(EditText(context)).apply {
      textSize = 14f
      background = null
      themeAware {
        textColor = it.textColorPrimary
      }
      updatePadding(top = dp(24), bottom = dp(8), left = dp(16), right = dp(16))
    })

    endIconMode = END_ICON_CLEAR_TEXT
    importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS

    boxBackgroundMode = BOX_BACKGROUND_FILLED
    themeAware {
      hintTextColor = ColorStateList.valueOf(it.accentColor)
      boxBackgroundColor = it.window.backgroundColor.blendWith(BLACK, ratio = 0.1f)
      boxStrokeColor = it.accentColor
      setEndIconTintList(hintTextColor)
    }
  }
}
