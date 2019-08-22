@file:Suppress("MayBeConstant")

package me.saket.wysiwyg

actual object Platform {
    actual val name: String = "iOS"
}

actual class SingleThreadBackgroundExecutor {
  actual fun <R> enqueue(runnable: () -> R) {
    TODO()
  }
}

actual object UiThreadExecutor {
  actual fun post(runnable: () -> Unit) {
    TODO()
  }
}