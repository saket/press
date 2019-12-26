package me.saket.wysiwyg

expect class SingleThreadBackgroundExecutor() {
  fun <R> enqueue(runnable: () -> R)
}

expect object UiThreadExecutor {
  fun enqueue(runnable: () -> Unit)
}
