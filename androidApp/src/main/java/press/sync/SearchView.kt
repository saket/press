package press.sync

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_FILLED
import com.google.android.material.textfield.TextInputLayout.END_ICON_CLEAR_TEXT
import me.saket.press.shared.localization.strings
import me.saket.press.shared.theme.blendWith
import press.extensions.textColor
import press.extensions.updatePadding
import press.theme.themeAware
import press.theme.themed
import press.widgets.dp

class SearchView(context: Context) : TextInputLayout(context) {
  private val editTextView = themed(EditText(context)).apply {
    textSize = 14f
    background = null
    themeAware {
      textColor = it.textColorPrimary
    }
    updatePadding(top = dp(24), bottom = dp(8), left = dp(16), right = dp(16))
  }

  init {
    addView(editTextView)
    endIconMode = END_ICON_CLEAR_TEXT
    importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS

    boxBackgroundMode = BOX_BACKGROUND_FILLED
    themeAware {
      hintTextColor = ColorStateList.valueOf(it.accentColor)
      boxBackgroundColor = it.window.backgroundColor.blendWith(Color.BLACK, ratio = 0.1f)
      boxStrokeColor = it.accentColor
    }
  }
}
