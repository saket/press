package press.home

import android.content.Context
import android.view.Gravity.CENTER
import com.squareup.contour.ContourLayout
import me.saket.press.shared.home.HomeModel.EmptyStateKind
import me.saket.press.shared.home.HomeModel.EmptyStateKind.Notes
import me.saket.press.shared.home.HomeModel.EmptyStateKind.Search
import me.saket.press.shared.localization.strings
import me.saket.press.shared.theme.TextStyles.mainBody
import me.saket.press.shared.theme.TextView
import press.theme.themePalette

class EmptyStateView(context: Context) : ContourLayout(context) {
  private val titleView = TextView(context, mainBody).apply {
    gravity = CENTER
    setTextColor(themePalette().textColorPrimary)
  }

  init {
    titleView.layoutBy(
      x = leftTo { parent.left() + 24.xdip }.rightTo { parent.right() - 24.xdip },
      y = topTo { parent.top() }
    )
    contourHeightOf { titleView.bottom() }
  }

  fun render(model: EmptyStateKind?) {
    titleView.text = when (model) {
      Search -> context.strings().home.emptystate_for_search
      Notes -> context.strings().home.emptystate_for_notes
      null -> null
    }
  }
}
