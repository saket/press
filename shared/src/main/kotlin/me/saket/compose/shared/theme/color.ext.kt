package me.saket.compose.shared.theme

import android.graphics.Color

actual fun String.toColor(): Int = Color.parseColor(this)