package me.saket.wysiwyg.util

import timber.log.Timber as AndroidTimber

actual object Timber {
  actual fun i(message: String) = AndroidTimber.i(message)
  actual fun d(message: String) = AndroidTimber.d(message)
}