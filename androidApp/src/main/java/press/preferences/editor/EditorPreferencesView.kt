package press.preferences.editor

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.LinearLayout.SHOW_DIVIDER_MIDDLE
import android.widget.LinearLayout.VERTICAL
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import me.saket.cascade.CascadePopupMenu
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.preferences.UserPreferences
import me.saket.press.shared.theme.UiStyles.Typeface
import press.preferences.PreferenceRowView
import press.theme.pressCascadeStyler
import press.theme.themeAware
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
    showDividers = SHOW_DIVIDER_MIDDLE
    updatePadding(bottom = dp(24))
    themeAware { dividerDrawable = DividerDrawable(it.separator) }
  }

  private val previewView = EditorPreviewView(context)
  private val fontFamilyView = PreferenceRowView(context)

  init {
    id = R.id.editor_preferences_view
    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }

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
    }

    fontFamilyView.render(
      setting = userPreferences.typeface,
      title = context.strings().prefs.editor_typeface,
      subtitle = { it!!.displayName },
      onClick = {
        val cascade = CascadePopupMenu(context, anchor = fontFamilyView, styler = pressCascadeStyler())
        Typeface.values().forEach {
          cascade.menu.add(it.displayName).setOnMenuItemClickListener {
            Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show()
            true
          }
        }
        cascade.show()
      }
    )
  }
}
