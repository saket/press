package press.editor

import android.graphics.Point
import android.graphics.PointF
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.widget.ScrollView
import android.widget.TextView
import me.saket.bettermovementmethod.BetterLinkMovementMethod

class EditorLinkMovementMethod(scrollView: ScrollView) : BetterLinkMovementMethod() {

  lateinit var clickedUrlCoordinates: Point

  init {
    // The text field can be longer than the screen size. Adjust for its scroll.
    setOnLinkClickListener { view, url ->
      val location = Point(
          clickedUrlCoordinates.x + scrollView.scrollX,
          clickedUrlCoordinates.y + scrollView.scrollY
      )
      UrlPopupMenu(view.context, view, url).showAt(location)
      true
    }
  }

  override fun onTouchEvent(view: TextView, text: Spannable, event: MotionEvent): Boolean {
    if (event.action == ACTION_UP) {
      clickedUrlCoordinates = Point(
          event.rawX.toInt(),
          event.rawY.toInt()
      )
    }
    return super.onTouchEvent(view, text, event)
  }
}
