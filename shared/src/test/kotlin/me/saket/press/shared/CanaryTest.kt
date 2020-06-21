package me.saket.press.shared

import me.saket.press.shared.PlatformHost.Android
import org.junit.Test
import kotlin.test.assertEquals

class CanaryTest {

  @Test fun canary() {
    assertEquals(Platform.host, Android)
  }
}
