package compose.home

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils.TruncateAt.END
import android.view.View
import android.widget.TextView
import com.squareup.contour.ContourLayout
import compose.util.y
import me.saket.compose.shared.note.Note

class NoteRowView(context: Context) : ContourLayout(context) {

  private val titleView = TextView(context).apply {
    textSize = 16f
    maxLines = 1
    ellipsize = END
    applyLayout(
        x = leftTo { parent.left() + 16.dip }.rightTo { parent.right() - 16.dip },
        y = topTo { parent.top() + 16.dip }
    )
  }

  private val bodyView = TextView(context).apply {
    textSize = 16f
    maxLines = 2
    ellipsize = END
    applyLayout(
        x = leftTo { titleView.left() }.rightTo { titleView.right() },
        y = topTo { titleView.bottom() + 16.dip }
    )
  }

  private val separatorView = View(context).apply {
    background = ColorDrawable(Color.parseColor("#62677C"))
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { bodyView.bottom() + 16.dip }.heightOf { 1.dip.y }
    )
  }

  init {
    heightOf { separatorView.bottom() }
  }

  fun render(note: Note) {
    titleView.text = note.title
    bodyView.text = note.body
  }
}