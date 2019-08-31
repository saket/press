package me.saket.wysiwyg.theme

import android.content.Context
import android.util.TypedValue

actual class DisplayUnits(private val context: Context) {

  actual fun fromPixels(px: Int): Float =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        px.toFloat(),
        context.resources.displayMetrics
    )
}