package press.sync

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color.BLACK
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN
import android.widget.EditText
import androidx.core.view.updatePadding
import com.google.android.material.textfield.TextInputLayout
import me.saket.press.R
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.theme.blendWith
import press.extensions.textColor
import press.theme.themeAware
import press.widgets.MaterialTextInputLayout
import press.widgets.dp

class SearchView(context: Context) : MaterialTextInputLayout(context) {
  init {
    editText.id = R.id.search_view_field
    editText.applyStyle(smallBody)
    endIconMode = END_ICON_CLEAR_TEXT

    themeAware {
      editText.textColor = it.textColorPrimary

      // Note to self: this is not the same as hintTextColor
      setEndIconTintList(ColorStateList.valueOf(editText.currentHintTextColor))
    }
  }
}
