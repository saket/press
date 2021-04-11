package press.preferences

import android.content.Context
import androidx.core.view.isVisible
import com.jakewharton.rxbinding3.view.attaches
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import me.saket.press.shared.listen
import me.saket.press.shared.preferences.Setting
import me.saket.press.shared.theme.TextStyles
import me.saket.press.shared.theme.TextView
import press.extensions.rippleDrawable
import press.extensions.textColor
import press.theme.themeAware

class PreferenceRowView(context: Context) : ContourLayout(context) {
  private val titleView = TextView(context, TextStyles.mainTitle)
  private val subtitleView = TextView(context, TextStyles.smallBody)

  init {
    titleView.layoutBy(
      x = matchParentX(marginLeft = 20.dip, marginRight = 20.dip),
      y = topTo { parent.top() + 16.dip }
    )
    subtitleView.layoutBy(
      x = matchXTo(titleView),
      y = topTo { titleView.bottom() }
    )
    contourHeightOf {
      (if (subtitleView.isVisible) subtitleView.bottom() else titleView.bottom()) + 16.ydip
    }

    themeAware {
      titleView.textColor = it.textColorPrimary
      subtitleView.textColor = it.textColorSecondary
      background = rippleDrawable(it)
    }
  }

  fun render(title: String, subtitle: String? = null, onClick: () -> Unit) {
    titleView.text = title
    subtitleView.text = subtitle
    subtitleView.isVisible = subtitle != null
    setOnClickListener { onClick() }
  }

  fun <T : Any> render(
    setting: Setting<T>,
    title: String,
    subtitle: (T) -> String,
    onClick: () -> Unit
  ) {
    render(title, subtitle = "", onClick)

    attaches()
      .switchMap { setting.listen() }
      .takeUntil(detaches())
      .subscribe { (preference) ->
        render(title, subtitle(preference!!), onClick)
      }
  }
}
