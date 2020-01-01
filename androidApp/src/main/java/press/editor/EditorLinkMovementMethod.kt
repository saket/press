package press.editor

import android.graphics.Point
import android.text.Selection
import android.text.Spannable
import android.text.method.MetaKeyKeyListener
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

// ===============================================================
// Functions copied from ArrowKeyMovementMethod because
// LinkMovementMethod focuses links when arrow keys are pressed.
// ===============================================================

  override fun left(widget: TextView, buffer: Spannable): Boolean {
    val layout = widget.layout
    return if (isSelecting(buffer)) {
      Selection.extendLeft(buffer, layout)
    } else {
      Selection.moveLeft(buffer, layout)
    }
  }

  override fun right(widget: TextView, buffer: Spannable): Boolean {
    val layout = widget.layout
    return if (isSelecting(buffer)) {
      Selection.extendRight(buffer, layout)
    } else {
      Selection.moveRight(buffer, layout)
    }
  }

  override fun up(widget: TextView, buffer: Spannable): Boolean {
    val layout = widget.layout
    return if (isSelecting(buffer)) {
      Selection.extendUp(buffer, layout)
    } else {
      Selection.moveUp(buffer, layout)
    }
  }

  override fun down(widget: TextView, buffer: Spannable): Boolean {
    val layout = widget.layout
    return if (isSelecting(buffer)) {
      Selection.extendDown(buffer, layout)
    } else {
      Selection.moveDown(buffer, layout)
    }
  }

  private fun isSelecting(buffer: Spannable): Boolean {
    return MetaKeyKeyListener.getMetaState(buffer, MetaKeyKeyListener.META_SHIFT_ON) == 1
  }
}
