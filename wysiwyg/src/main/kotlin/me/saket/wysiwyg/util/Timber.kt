package me.saket.wysiwyg.util

import android.annotation.SuppressLint
import android.util.Log

@SuppressLint("LogNotTimber")
actual object Timber {

  actual fun i(message: String) {
    Log.i("Compose", message)
  }

  actual fun d(message: String) {
    Log.d("Compose", message)
  }
}