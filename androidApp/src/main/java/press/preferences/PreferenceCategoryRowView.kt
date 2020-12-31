package press.preferences

import android.animation.AnimatorInflater
import android.content.Context
import com.squareup.contour.ContourLayout
import me.saket.press.R.animator
import me.saket.press.shared.home.HomeUiStyles
import me.saket.press.shared.preferences.PreferenceCategoryItemModel
import me.saket.press.shared.theme.TextView
import press.extensions.textColor
import press.theme.themeAware

class PreferenceCategoryRowView(context: Context) : ContourLayout(context) {
  private val titleView = TextView(context, HomeUiStyles.noteTitle).apply {
    themeAware {
      textColor = it.textColorHeading
    }
  }

  private val subtitleView = TextView(context, HomeUiStyles.noteBody).apply {
    themeAware {
      textColor = it.textColorSecondary
    }
  }

  lateinit var model: PreferenceCategoryItemModel

  init {
    titleView.layoutBy(
      x = leftTo { parent.left() + 16.dip }.rightTo { parent.right() - 16.dip },
      y = topTo { parent.top() + 16.dip }
    )
    subtitleView.layoutBy(
      x = leftTo { titleView.left() }.rightTo { titleView.right() },
      y = topTo { titleView.bottom() + 8.dip }
    )

    stateListAnimator = AnimatorInflater.loadStateListAnimator(context, animator.thread_elevation_stateanimator)
    contourHeightOf { subtitleView.bottom() + 16.dip }
    themeAware {
      setBackgroundColor(it.window.elevatedBackgroundColor)
    }
  }

  fun render(model: PreferenceCategoryItemModel) {
    this.model = model
    titleView.text = model.title
    subtitleView.text = model.subtitle
  }
}
