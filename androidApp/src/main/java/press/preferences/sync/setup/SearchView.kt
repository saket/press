package press.preferences.sync.setup

import android.content.Context
import android.content.res.ColorStateList
import me.saket.press.R
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.applyStyle
import press.extensions.textColor
import press.theme.themePalette
import press.widgets.MaterialTextInputLayout

class SearchView(context: Context) : MaterialTextInputLayout(context) {
  init {
    editText.id = R.id.search_view_field
    editText.applyStyle(smallBody)
    endIconMode = END_ICON_CLEAR_TEXT
    editText.textColor = themePalette().textColorPrimary

    // Note to self: this is not the same as hintTextColor
    setEndIconTintList(ColorStateList.valueOf(editText.currentHintTextColor))
  }
}
