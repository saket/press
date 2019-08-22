@file:Suppress("MayBeConstant")

package me.saket.wysiwyg

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

actual object Platform {
  actual val name: String = "Android"
}

actual class SingleThreadBackgroundExecutor {
  private val executor = Executors.newSingleThreadExecutor()

  actual fun <R> enqueue(runnable: () -> R) {
    // submit().get() will bubble up exceptions.
    // execute() will swallow exceptions in bg threads.
    executor.submit(runnable).get()
  }
}

actual object UiThreadExecutor {
  private val handler = Handler(Looper.getMainLooper())

  actual fun post(runnable: () -> Unit) {
    handler.post(runnable)
  }
}