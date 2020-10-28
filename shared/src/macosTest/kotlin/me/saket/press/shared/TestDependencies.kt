package me.saket.press.shared

import com.soywiz.klock.DateTime
import kotlinx.cinterop.toKString
import kotlinx.coroutines.CoroutineScope
import me.saket.press.shared.sync.git.DeviceInfo
import me.saket.press.shared.sync.git.File
import me.saket.wysiwyg.atomicLazy
import platform.posix.getenv

actual fun testDeviceInfo() = object : DeviceInfo {
  override val appStorage: File by atomicLazy {
    val tempPath = getenv("TMPDIR")!!.toKString()
    File(tempPath + "press_${DateTime.nowUnixLong()}").makeDirectory()
  }

  override fun deviceName() = "Test"
}

actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T {
  return kotlinx.coroutines.runBlocking {
    block(this)
  }
}
