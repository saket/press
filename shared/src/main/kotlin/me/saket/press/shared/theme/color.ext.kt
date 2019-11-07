package me.saket.press.shared.theme

import android.graphics.Color

actual fun String.toColor(): Int = Color.parseColor(this)