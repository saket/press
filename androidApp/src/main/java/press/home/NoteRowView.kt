package press.home

import android.animation.AnimatorInflater.loadStateListAnimator
import android.content.Context
import com.squareup.contour.ContourLayout
import me.saket.press.R
import me.saket.press.shared.home.HomeModel
import me.saket.press.shared.home.HomeUiStyles.noteBody
import me.saket.press.shared.home.HomeUiStyles.noteTitle
import me.saket.press.shared.theme.TextView
import press.extensions.textColor
import press.theme.themeAware

class NoteRowView(context: Context) : ContourLayout(context) {
  private val titleView = TextView(context, noteTitle).apply {
    themeAware {
      textColor = it.textColorHeading
    }
  }

  private val bodyView = TextView(context, noteBody).apply {
    themeAware {
      textColor = it.textColorSecondary
    }
  }

  lateinit var model: HomeModel.NoteModel

  init {
    titleView.layoutBy(
      x = leftTo { parent.left() + 16.dip }.rightTo { parent.right() - 16.dip },
      y = topTo { parent.top() + 16.dip }
    )
    bodyView.layoutBy(
      x = leftTo { titleView.left() }.rightTo { titleView.right() },
      y = topTo { titleView.bottom() + 8.dip }
    )

    stateListAnimator = loadStateListAnimator(context, R.animator.thread_elevation_stateanimator)
    contourHeightOf { bodyView.bottom() + 16.dip }
    themeAware {
      setBackgroundColor(it.window.elevatedBackgroundColor)
    }
  }

  fun render(model: HomeModel.NoteModel) {
    this.model = model
    titleView.text = model.title
    bodyView.text = model.body
  }
}
