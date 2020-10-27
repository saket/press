package me.saket.wysiwyg

actual class SingleThreadBackgroundExecutor {
  actual fun <R> enqueue(runnable: () -> R) {
    TODO()
  }
}

actual object UiThreadExecutor {
  actual fun enqueue(runnable: () -> Unit) {
    TODO()
  }
}
