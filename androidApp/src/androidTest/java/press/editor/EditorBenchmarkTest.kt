package press.editor

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditorBenchmarkTest {

  @Test
  fun testEnvironmentWorks() {
    val context = InstrumentationRegistry.getInstrumentation().context
    assertThat(context).isNotNull()
  }
}
