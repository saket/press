package press.preferences.sync.setup

import android.content.Context
import android.text.style.ForegroundColorSpan
import android.view.View
import com.squareup.contour.ContourLayout
import me.saket.press.shared.preferences.sync.setup.RepoUiModel
import me.saket.press.shared.theme.TextStyles.mainTitle
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import press.extensions.rippleDrawable
import press.extensions.textColor
import press.theme.themePalette
import press.widgets.withSpan

class GitRepoRowView(context: Context) : ContourLayout(context) {
  private val ownerView = TextView(context, smallBody).apply {
    textColor = themePalette().textColorSecondary
    applyLayout(
      x = matchParentX(marginLeft = 22.dip, marginRight = 22.dip),
      y = topTo { parent.top() + 16.ydip }
    )
  }

  private val nameView = TextView(context, mainTitle).apply {
    textColor = themePalette().textColorPrimary
    applyLayout(
      x = matchParentX(marginLeft = 22.dip, marginRight = 22.dip),
      y = topTo { ownerView.bottom() }
    )
  }

  private val dividerView = View(context).apply {
    setBackgroundColor(themePalette().divider)
    applyLayout(
      x = matchParentX(),
      y = topTo { nameView.bottom() + 16.ydip }.heightOf { 1.ydip }
    )
  }

  init {
    contourHeightOf { dividerView.bottom() }

    // Prevent RecyclerView items from leaking through each other by giving this a solid color.
    setBackgroundColor(themePalette().window.backgroundColor)
    foreground = rippleDrawable()
  }

  fun render(model: RepoUiModel) {
    val highlightSpan = ForegroundColorSpan(themePalette().accentColor)
    ownerView.text = model.owner.withSpan(highlightSpan)
    nameView.text = model.name.withSpan(highlightSpan)
  }
}
