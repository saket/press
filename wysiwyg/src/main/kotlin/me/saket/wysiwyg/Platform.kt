@file:Suppress("MayBeConstant")

package me.saket.wysiwyg

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

actual class SingleThreadBackgroundExecutor: Executor {
  private val executor = Executors.newSingleThreadExecutor()

  override fun enqueue(runnable: () -> Unit) {
    // submit().get() will bubble up exceptions.
    // execute() will swallow exceptions in bg threads.
    executor.submit(runnable).get()
  }
}

actual object UiThreadExecutor : Executor {
  private val handler = when {
    SDK_INT >= P -> Handler.createAsync(Looper.getMainLooper())
    else -> Handler(Looper.getMainLooper())
  }

  override fun enqueue(runnable: () -> Unit) {
    handler.post(runnable)
  }
}
