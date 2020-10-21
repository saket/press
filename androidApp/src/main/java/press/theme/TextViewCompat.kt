package press.theme

import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import press.extensions.reflect

object TextViewCompat {

  fun textSelectionHandles(view: TextView): Array<Drawable> {
    if (VERSION.SDK_INT >= 29) {
      return arrayOf(view.textSelectHandle!!, view.textSelectHandleLeft!!, view.textSelectHandleRight!!)

    } else {
      val centerDrawableResId = reflect(TextView::class, "mTextSelectHandleRes")?.getInt(view) ?: return emptyArray()
      val leftDrawableResId = reflect(TextView::class, "mTextSelectHandleLeftRes")!!.getInt(view)
      val rightDrawableResId = reflect(TextView::class, "mTextSelectHandleRightRes")!!.getInt(view)

      val editorField = reflect(TextView::class, "mEditor")
      val editor: Any = editorField!!.get(view) ?: return emptyArray()

      val centerDrawable = getDrawable(view.context, centerDrawableResId)!!
      val leftDrawable = getDrawable(view.context, leftDrawableResId)!!
      val rightDrawable = getDrawable(view.context, rightDrawableResId)!!

      reflect(editor::class, "mSelectHandleCenter")?.set(editor, centerDrawable) ?: return emptyArray()
      reflect(editor::class, "mSelectHandleLeft")!!.set(editor, leftDrawable)
      reflect(editor::class, "mSelectHandleRight")!!.set(editor, rightDrawable)

      return arrayOf(centerDrawable, leftDrawable, rightDrawable)
    }
  }
}
