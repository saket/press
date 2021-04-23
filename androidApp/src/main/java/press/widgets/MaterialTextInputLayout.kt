package press.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.view.updatePadding
import com.google.android.material.textfield.TextInputLayout
import me.saket.press.shared.theme.blendWith
import press.theme.themePalette

@Suppress("LeakingThis")
open class MaterialTextInputLayout(context: Context) : TextInputLayout(context) {
  init {
    addView(
      EditText(context).apply {
        background = null
        imeOptions = imeOptions or EditorInfo.IME_FLAG_NO_FULLSCREEN
        updatePadding(top = dp(24), bottom = dp(8), left = dp(16), right = dp(16))
      }
    )

    importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
    boxBackgroundMode = BOX_BACKGROUND_FILLED

    val palette = themePalette()
    hintTextColor = ColorStateList.valueOf(palette.accentColor)
    boxBackgroundColor = palette.window.backgroundColor.blendWith(Color.BLACK, ratio = 0.1f)
    boxStrokeColor = palette.accentColor
    setHelperTextColor(ColorStateList.valueOf(palette.textColorHint))
  }

  override fun getEditText(): EditText {
    return super.getEditText()!!
  }
}
