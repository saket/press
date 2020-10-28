package me.saket.press.shared

import kotlinx.cinterop.toKString
import kotlinx.coroutines.CoroutineScope
import me.saket.press.shared.sync.git.DeviceInfo
import me.saket.press.shared.sync.git.File
import platform.posix.getenv

actual fun testDeviceInfo() = object : DeviceInfo {
  override val appStorage: File get() = File(getenv("TMPDIR")!!.toKString())
  override fun deviceName() = "Test"
}

actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T {
  return kotlinx.coroutines.runBlocking {
    block(this)
  }
}
