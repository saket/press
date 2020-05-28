package me.saket.press.shared.sync

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import me.saket.press.shared.sync.git.AppStorage
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])  // API 29 requires Java 9.
class AndroidGitSyncerTest : GitSyncerTest(
    AppStorage(path = ApplicationProvider.getApplicationContext<Context>().filesDir.path)
)
