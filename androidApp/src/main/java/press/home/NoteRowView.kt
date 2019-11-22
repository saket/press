package press.home

import android.animation.AnimatorInflater.loadStateListAnimator
import android.content.Context
import android.text.TextUtils.TruncateAt.END
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.squareup.contour.ContourLayout
import me.saket.press.R
import press.theme.themeAware
import press.theme.themed
import press.widgets.textColor
import me.saket.press.R.animator
import me.saket.press.shared.home.HomeUiModel
import me.saket.press.shared.theme.toColor

class NoteRowView(context: Context) : ContourLayout(context) {

  private val titleView = themed(TextView(context)).apply {
    textSize = 16f
    maxLines = 1
    ellipsize = END
    typeface = ResourcesCompat.getFont(context, R.font.work_sans_bold)
    themeAware {
      textColor = it.textColorHeading
    }
    applyLayout(
        x = leftTo { parent.left() + 16.dip }.rightTo { parent.right() - 16.dip },
        y = topTo { parent.top() + 16.dip }
    )
  }

  private val bodyView = themed(TextView(context)).apply {
    textSize = 15f
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

  lateinit var noteModel: HomeUiModel.Note

  init {
    contourHeightOf { bodyView.bottom() + 16.dip }

    stateListAnimator = loadStateListAnimator(context, animator.thread_elevation_stateanimator)
    themeAware { setBackgroundColor("#393c4b".toColor()) }  // White with 2% opacity.
  }

  fun render(noteModel: HomeUiModel.Note) {
    this.noteModel = noteModel
    titleView.text = noteModel.title
    bodyView.text = noteModel.body
  }
}
