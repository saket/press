package me.saket.press.shared

import com.soywiz.klock.DateTime
import kotlinx.cinterop.toKString
import me.saket.press.shared.syncer.git.DeviceInfo
import me.saket.press.shared.syncer.git.File
import me.saket.wysiwyg.atomicLazy
import platform.posix.getenv

actual fun testDeviceInfo() = object : DeviceInfo {
  override val appStorage: File by atomicLazy {
    val tempPath = getenv("TMPDIR")!!.toKString()
    File(tempPath + "press_${DateTime.nowUnixLong()}/").makeDirectories()
  }

  override fun deviceName() = "Test"

  override fun supportsSplitScreen(): Boolean = false
}
