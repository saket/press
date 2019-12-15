package me.saket.press.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class CanaryTest {

  @Test fun canary() {
    assertEquals(Platform.name, "iOS")
  }
}
