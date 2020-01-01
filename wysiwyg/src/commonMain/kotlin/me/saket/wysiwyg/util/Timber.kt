package me.saket.wysiwyg.util

internal expect object Timber {
  inline fun i(message: String)
  inline fun d(message: String)
  inline fun w(message: String)
}
