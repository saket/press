package me.saket.press.shared

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.saket.press.shared.sync.git.DeviceInfo
import me.saket.press.shared.sync.git.File
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
actual abstract class RobolectricTest

actual fun testDeviceInfo() = DeviceInfo(
    appStorage = File(ApplicationProvider.getApplicationContext<Context>().filesDir.path),
    deviceName = { "Test" }
)
