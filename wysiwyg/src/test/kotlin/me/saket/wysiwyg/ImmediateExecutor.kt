package me.saket.wysiwyg

class ImmediateExecutor : Executor {
  override fun enqueue(runnable: () -> Unit) = runnable()
}
