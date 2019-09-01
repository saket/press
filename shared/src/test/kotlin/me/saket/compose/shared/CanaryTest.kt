package me.saket.compose.shared

import org.junit.Test
import kotlin.test.assertEquals

class CanaryTest {

  @Test fun foo() {
    assertEquals(Platform.name, "Android")
  }
}