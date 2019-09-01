package compose.home

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.squareup.contour.ContourLayout

class HomeView(context: Context) : ContourLayout(context) {

  private val notesList = RecyclerView(context).apply {
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.right() },
        y = topTo { parent.top() }.bottomTo { parent.bottom() }
    )
  }
}