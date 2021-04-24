package press.preferences.editor

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.LinearLayout.SHOW_DIVIDER_END
import android.widget.LinearLayout.SHOW_DIVIDER_MIDDLE
import android.widget.LinearLayout.VERTICAL
import android.widget.Toast
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import me.saket.cascade.CascadePopupMenu
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.preferences.AutoCorrectEnabled
import me.saket.press.shared.preferences.UserPreferences
import me.saket.press.shared.theme.TextStyles.mainTitle
import me.saket.press.shared.theme.UiStyles.Typeface
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.theme.asAndroidTypeface
import press.extensions.updatePadding
import press.preferences.TwoLinePreferenceView
import press.theme.pressCascadeStyler
import press.theme.themePalette
import press.widgets.DividerDrawable
import press.widgets.PressToolbar
import press.widgets.dp

class EditorPreferencesView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  userPreferences: UserPreferences
) : ContourLayout(context) {

  private val toolbar = PressToolbar(context).apply {
    title = context.strings().prefs.category_title_editor
  }

  private val preferenceList = LinearLayout(context).apply {
    orientation = VERTICAL
    showDividers = SHOW_DIVIDER_MIDDLE or SHOW_DIVIDER_END
    dividerDrawable = DividerDrawable(themePalette().divider)
    updatePadding(bottom = dp(24))
  }

  private val previewView = EditorPreviewView(context)
  private val fontFamilyView = TwoLinePreferenceView(context)

  private val autoCorrectToggleView = SwitchMaterial(context).apply {
    applyStyle(mainTitle)
    updatePadding(horizontal = 20.dip, vertical = 20.dip)
  }

  init {
    id = R.id.editor_preferences_view
    setBackgroundColor(themePalette().window.backgroundColor)

    toolbar.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
    NestedScrollView(context).apply {
      addView(preferenceList)
      layoutBy(
        x = matchParentX(),
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
      )
    }

    preferenceList.let {
      it.addView(previewView)
      it.addView(fontFamilyView)
      it.addView(autoCorrectToggleView)
    }

    val strings = context.strings().prefs
    fontFamilyView.render(
      setting = userPreferences.typeface,
      title = strings.editor_typeface,
      subtitle = { it.styledDisplayName() },
      onClick = {
        CascadePopupMenu(context, anchor = fontFamilyView, styler = pressCascadeStyler()).apply {
          Typeface.values().forEach {
            menu.add(it.styledDisplayName()).setOnMenuItemClickListener {
              Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show()
              true
            }
          }
        }.show()
      }
    )

    autoCorrectToggleView.text = strings.editor_autocorrect
    autoCorrectToggleView.isChecked = userPreferences.autoCorrectEnabled.get()!!.enabled
    autoCorrectToggleView.setOnCheckedChangeListener { _, isChecked ->
      userPreferences.autoCorrectEnabled.set(AutoCorrectEnabled(enabled = isChecked))
    }
  }

  private fun Typeface.styledDisplayName(): CharSequence {
    val typeface = this
    return buildSpannedString {
      inSpans(Api26TypefaceSpan(typeface.asAndroidTypeface(context))) {
        append(typeface.displayName)
      }
    }
  }
}
