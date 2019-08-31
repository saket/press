package me.saket.wysiwyg.theme

import android.graphics.Color

actual fun String.toHexColor(): Int = Color.parseColor(this)