package press.preferences

import android.content.Context
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.squareup.contour.ContourLayout
import me.saket.press.R
import me.saket.press.shared.home.HomeUiStyles
import me.saket.press.shared.preferences.PreferenceCategory.AboutApp
import me.saket.press.shared.preferences.PreferenceCategory.Editor
import me.saket.press.shared.preferences.PreferenceCategory.LookAndFeel
import me.saket.press.shared.preferences.PreferenceCategory.Sync
import me.saket.press.shared.preferences.PreferenceCategoryItemModel
import me.saket.press.shared.theme.TextStyles
import me.saket.press.shared.theme.TextStyles.mainTitle
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import press.extensions.createRippleDrawable
import press.extensions.textColor
import press.theme.themeAware

class PreferenceCategoryRowView(context: Context) : ContourLayout(context) {
  private val iconView = AppCompatImageView(context)
  private val titleView = TextView(context, mainTitle)
  private val subtitleView = TextView(context, smallBody)

  lateinit var model: PreferenceCategoryItemModel

  init {
    iconView.layoutBy(
      x = leftTo { parent.left() + 16.dip }.widthOf { 24.xdip },
      y = centerVerticallyTo { parent.centerY() }.heightOf { 24.ydip }
    )
    titleView.layoutBy(
      x = leftTo { iconView.right() + 16.dip }.rightTo { parent.right() - 16.dip },
      y = topTo { parent.top() + 16.dip }
    )
    subtitleView.layoutBy(
      x = matchXTo(titleView),
      y = topTo { titleView.bottom() }
    )
    contourHeightOf { subtitleView.bottom() + 16.dip }

    themeAware {
      iconView.setColorFilter(it.accentColor)
      titleView.textColor = it.textColorPrimary
      subtitleView.textColor = it.textColorSecondary
      background = createRippleDrawable(it)
    }
  }

  fun render(model: PreferenceCategoryItemModel) {
    this.model = model
    iconView.setImageResource(
      when(model.category) {
        LookAndFeel -> R.drawable.ic_twotone_format_24dp
        Editor -> R.drawable.ic_twotone_format_shapes_24
        Sync -> R.drawable.ic_twotone_phonelink_24
        AboutApp -> R.drawable.ic_twotone_adb_24
      }
    )
    titleView.text = model.title
    subtitleView.text = model.subtitle
  }
}
