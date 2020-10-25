package me.saket.press.shared

import kotlinx.coroutines.CoroutineScope
import me.saket.press.shared.sync.git.DeviceInfo

actual fun testDeviceInfo(): DeviceInfo = TODO()

actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T {
  return kotlinx.coroutines.runBlocking {
    block(this)
  }
}
