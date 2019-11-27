package press.home

import android.animation.AnimatorInflater.loadStateListAnimator
import android.content.Context
import android.widget.TextView
import com.squareup.contour.ContourLayout
import me.saket.press.R
import me.saket.press.shared.home.HomeUiModel
import me.saket.press.shared.theme.HomeUiStyles
import me.saket.press.shared.theme.applyStyle
import press.theme.themeAware
import press.theme.themed
import press.widgets.textColor

class NoteRowView(context: Context) : ContourLayout(context) {

  private val titleView = themed(TextView(context)).apply {
    HomeUiStyles.NoteRow.title.applyStyle(this)
    themeAware {
      textColor = it.textColorHeading
    }
    applyLayout(
        x = leftTo { parent.left() + 16.dip }.rightTo { parent.right() - 16.dip },
        y = topTo { parent.top() + 16.dip }
    )
  }

  private val bodyView = themed(TextView(context)).apply {
    HomeUiStyles.NoteRow.body.applyStyle(this)
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

    stateListAnimator = loadStateListAnimator(context, R.animator.thread_elevation_stateanimator)

    themeAware {
      setBackgroundColor(it.window.editorBackgroundColor)
    }
  }

  fun render(noteModel: HomeUiModel.Note) {
    this.noteModel = noteModel
    titleView.text = noteModel.title
    bodyView.text = noteModel.body
  }
}
