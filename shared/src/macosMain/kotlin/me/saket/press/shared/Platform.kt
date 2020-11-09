package me.saket.press.shared

import kotlinx.coroutines.CoroutineScope

actual object Platform {
  actual val host = PlatformHost.macOS
}

actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T {
  return kotlinx.coroutines.runBlocking {
    block(this)
  }
}

actual interface AndroidParcel
