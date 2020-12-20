package press.home

import android.animation.AnimatorInflater.loadStateListAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.Gravity.CENTER_VERTICAL
import android.widget.TextView
import com.squareup.contour.ContourLayout
import me.saket.press.R
import me.saket.press.shared.home.HomeUiModel
import me.saket.press.shared.home.HomeUiStyles.noteBody
import me.saket.press.shared.home.HomeUiStyles.noteTitle
import me.saket.press.shared.theme.TextView
import press.extensions.getDrawable
import press.extensions.textColor
import press.theme.themeAware

class FolderRowView(context: Context) : ContourLayout(context) {
  private val titleView = TextView(context, noteTitle).apply {
    gravity = CENTER_VERTICAL
    compoundDrawablePadding = 8.dip
    themeAware {
      textColor = it.textColorHeading
      setStartDrawable(context.getDrawable(R.drawable.ic_twotone_folder_16, tint = it.textColorHeading))
    }
  }

  lateinit var model: HomeUiModel.Folder

  init {
    titleView.layoutBy(
      x = leftTo { parent.left() + 16.dip }.rightTo { parent.right() - 16.dip },
      y = topTo { parent.top() + 16.dip }
    )

    stateListAnimator = loadStateListAnimator(context, R.animator.thread_elevation_stateanimator)
    contourHeightOf { titleView.bottom() + 16.dip }
    themeAware {
      setBackgroundColor(it.window.editorBackgroundColor)
    }
  }

  fun render(noteModel: HomeUiModel.Folder) {
    this.model = noteModel
    titleView.text = noteModel.title.capitalize()
  }
}

private fun TextView.setStartDrawable(icon: Drawable?) {
  setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
}
