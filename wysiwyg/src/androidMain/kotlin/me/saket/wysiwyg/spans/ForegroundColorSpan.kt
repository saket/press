package me.saket.wysiwyg.spans

import android.graphics.Color
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import androidx.annotation.ColorInt

class ForegroundColorSpan(val recycler: Recycler) :
  CharacterStyle(),
  UpdateAppearance,
  WysiwygSpan {

  @ColorInt var color: Int = Color.TRANSPARENT

  override fun updateDrawState(textPaint: TextPaint) {
    textPaint.color = color
  }

  override fun recycle() {
    recycler(this)
  }
}
