package me.saket.press.shared.sync

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import me.saket.press.shared.sync.git.DeviceInfo
import me.saket.press.shared.sync.git.File
import org.junit.FixMethodOrder
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P], manifest = Config.NONE)  // API 29 requires Java 9.
@FixMethodOrder(MethodSorters.JVM)
class AndroidGitSyncerTest : GitSyncerTest(
    deviceInfo = DeviceInfo(
        appStorage = File(ApplicationProvider.getApplicationContext<Context>().filesDir.path),
        deviceName = { "Test" }
    )
)
