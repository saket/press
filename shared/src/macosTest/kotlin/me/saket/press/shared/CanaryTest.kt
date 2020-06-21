package me.saket.press.shared

import me.saket.press.shared.PlatformHost.macOS
import kotlin.test.Test
import kotlin.test.assertEquals

class CanaryTest {

  @Test fun canary() {
    assertEquals(Platform.host, macOS)
  }
}
