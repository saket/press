package me.saket.press.shared

import me.saket.press.shared.syncer.git.DeviceInfo
import me.saket.press.shared.syncer.git.File
import java.nio.file.Files

actual fun testDeviceInfo() = object : DeviceInfo {
  override val appStorage get() = File(Files.createTempDirectory("press_").toString())
  override fun deviceName() = "Test"
  override fun supportsSplitScreen(): Boolean = false
}
