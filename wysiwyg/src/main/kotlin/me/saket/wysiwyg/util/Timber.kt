package me.saket.wysiwyg.util

import android.annotation.SuppressLint
import timber.log.Timber as JwTimber

@Suppress("NOTHING_TO_INLINE")
@SuppressLint("LogNotTimber")
internal actual object Timber {

  actual inline fun i(message: String) {
    JwTimber.i(message)
  }

  actual inline fun d(message: String) {
    JwTimber.d(message)
  }

  actual inline fun w(message: String) {
    JwTimber.w(message)
  }
}