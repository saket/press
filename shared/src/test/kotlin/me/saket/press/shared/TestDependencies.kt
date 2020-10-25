package me.saket.press.shared

import kotlinx.coroutines.CoroutineScope
import me.saket.press.shared.sync.git.DeviceInfo
import me.saket.press.shared.sync.git.File
import java.nio.file.Files

actual fun testDeviceInfo() = object : DeviceInfo {
  override val appStorage get() = File(Files.createTempDirectory("press_").toString())
  override fun deviceName() = "Test"
}

actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T {
  return kotlinx.coroutines.runBlocking {
    block(this)
  }
}
