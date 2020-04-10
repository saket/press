package me.saket.wysiwyg

interface Executor {
  fun enqueue(runnable: () -> Unit)
}

expect class SingleThreadBackgroundExecutor(): Executor
expect object UiThreadExecutor: Executor
