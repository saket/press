package press.sync

import android.content.Context
import android.view.View
import androidx.core.view.isGone
import com.squareup.contour.ContourLayout
import me.saket.press.R
import me.saket.press.shared.sync.git.service.GitRepositoryInfo
import me.saket.press.shared.theme.TextStyles
import press.extensions.TextView
import press.theme.themeAware
import press.extensions.attr
import press.extensions.textColor

class GitRepoItemView(context: Context) : ContourLayout(context) {

  private val ownerView = TextView(context, TextStyles.Secondary).apply {
    themeAware {
      textColor = it.textColorSecondary
    }
    applyLayout(
        x = matchParentX(marginLeft = 22.dip, marginRight = 22.dip),
        y = topTo { parent.top() + 16.ydip }
    )
  }

  private val nameView = TextView(context, TextStyles.Primary).apply {
    themeAware {
      textColor = it.textColorPrimary
    }
    applyLayout(
        x = matchParentX(marginLeft = 22.dip, marginRight = 22.dip),
        y = topTo { ownerView.bottom() }
    )
  }

  private val dividerView = View(context).apply {
    themeAware {
      setBackgroundColor(it.separator)
    }
    applyLayout(
        x = matchParentX(),
        y = topTo { nameView.bottom() + 16.ydip }.heightOf { 1.ydip }
    )
  }

  init {
    contourHeightOf { dividerView.bottom() }
    background = attr(R.attr.selectableItemBackground).asDrawable()
  }

  fun render(repo: GitRepositoryInfo, showDivider: Boolean) {
    ownerView.text = repo.owner
    nameView.text = repo.name
    dividerView.isGone = !showDivider
  }
}
