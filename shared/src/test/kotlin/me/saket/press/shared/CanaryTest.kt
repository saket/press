package me.saket.press.shared

import org.junit.Test
import kotlin.test.assertEquals

class CanaryTest {

  @Test fun canary() {
    assertEquals(Platform.name, "Android")
  }
}
