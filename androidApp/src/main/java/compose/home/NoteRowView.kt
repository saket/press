package compose.home

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils.TruncateAt.END
import android.view.View
import android.widget.TextView
import com.squareup.contour.ContourLayout
import compose.theme.themeAware
import compose.theme.themed
import compose.widgets.textColor
import compose.util.y
import me.saket.compose.shared.home.HomeUiModel
import me.saket.compose.shared.theme.toColor

class NoteRowView(context: Context) : ContourLayout(context) {

  private val titleView = themed(TextView(context)).apply {
    textSize = 16f
    maxLines = 1
    ellipsize = END
    themeAware {
      textColor = it.textColorHeading
    }
    applyLayout(
        x = leftTo { parent.left() + 16.dip }.rightTo { parent.right() - 16.dip },
        y = topTo { parent.top() + 16.dip }
    )
  }

  private val bodyView = themed(TextView(context)).apply {
    textSize = 16f
    maxLines = 2
    ellipsize = END
    themeAware {
      textColor = it.textColorSecondary
    }
    applyLayout(
        x = leftTo { titleView.left() }.rightTo { titleView.right() },
        y = topTo { titleView.bottom() + 8.dip }
    )
  }

  private val separatorView = View(context).apply {
    background = ColorDrawable("#62677C".toColor()) // TODO: get color from theme.
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { bodyView.bottom() + 16.dip }.heightOf { 1.dip.y }
    )
  }

  init {
    contourHeightOf { separatorView.bottom() }
  }

  fun render(note: HomeUiModel.Note) {
    titleView.text = note.title
    bodyView.text = note.body
  }
}