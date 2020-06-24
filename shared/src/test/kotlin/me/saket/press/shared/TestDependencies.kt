package me.saket.press.shared

import me.saket.press.shared.sync.git.DeviceInfo
import me.saket.press.shared.sync.git.File
import java.nio.file.Files

actual fun testDeviceInfo() = DeviceInfo(
    appStorage = File(Files.createTempDirectory("press_").toString()),
    deviceName = { "Test" }
)
