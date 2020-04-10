@file:Suppress("MayBeConstant")

package me.saket.wysiwyg

actual class SingleThreadBackgroundExecutor : Executor {
  override fun enqueue(runnable: () -> Unit) {
    TODO()
  }
}

actual object UiThreadExecutor : Executor {
  override fun enqueue(runnable: () -> Unit) {
    TODO()
  }
}
