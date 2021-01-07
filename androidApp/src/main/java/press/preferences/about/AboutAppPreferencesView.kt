package press.preferences.about

import android.content.Context
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.LinearLayout.SHOW_DIVIDER_END
import android.widget.LinearLayout.SHOW_DIVIDER_MIDDLE
import android.widget.LinearLayout.VERTICAL
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.theme.TextStyles.mainBody
import me.saket.press.shared.theme.TextView
import press.extensions.textColor
import press.navigation.navigator
import press.preferences.PreferenceRowView
import press.theme.themeAware
import press.widgets.DividerDrawable
import press.widgets.PressToolbar

class AboutAppPreferencesView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null
) : ContourLayout(context) {

  private val strings get() = context.strings().prefs

  private val toolbar = PressToolbar(context).apply {
    title = strings.category_title_about_app
  }

  private val headerTextView = TextView(context, mainBody).apply {
    text = Html.fromHtml(strings.about_header_html, FROM_HTML_MODE_LEGACY)
    movementMethod = BetterLinkMovementMethod.getInstance()
    updatePaddingRelative(start = 16.dip, end = 16.dip, top = 0, bottom = 24.dip)
  }

  private val playStoreView = PreferenceRowView(context)
  private val githubView = PreferenceRowView(context)
  private val creditsView = PreferenceRowView(context)

  private val preferenceList = LinearLayout(context).apply {
    orientation = VERTICAL
    showDividers = SHOW_DIVIDER_MIDDLE or SHOW_DIVIDER_END
    addView(headerTextView)
    addView(playStoreView)
    addView(githubView)
    addView(creditsView)
  }

  init {
    id = R.id.aboutapp_preferences_view
    toolbar.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
    ScrollView(context).apply {
      addView(preferenceList)
      layoutBy(
        x = matchParentX(),
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
      )
    }

    themeAware {
      setBackgroundColor(it.window.backgroundColor)
      preferenceList.dividerDrawable = DividerDrawable(it.separator)
      headerTextView.textColor = it.textColorPrimary
    }

    playStoreView.render(
      title = strings.about_playstore_link_title,
      subtitle = null,
      onClick = {
        navigator().intentLauncher().openUrl("https://play.google.com/store/apps/details?id=me.saket.press")
      }
    )
    githubView.render(
      title = strings.about_github_link_title,
      onClick = {
        navigator().intentLauncher().openUrl("https://github.com/saket/press")
      }
    )
    creditsView.render(
      title = strings.about_credits_title,
      onClick = {
        Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show()
      }
    )
  }
}
