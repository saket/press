@file:Suppress("MayBeConstant")

package me.saket.wysiwyg

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

actual class SingleThreadBackgroundExecutor {
  private val executor = Executors.newSingleThreadExecutor()

  actual fun <R> enqueue(runnable: () -> R) {
    // submit().get() will bubble up exceptions.
    // execute() will swallow exceptions in bg threads.
    executor.submit(runnable).get()
  }
}

actual object UiThreadExecutor {
  private val handler = when {
    SDK_INT >= P -> Handler.createAsync(Looper.getMainLooper())
    else -> Handler(Looper.getMainLooper())
  }

  actual fun enqueue(runnable: () -> Unit) {
    handler.post(runnable)
  }
}
