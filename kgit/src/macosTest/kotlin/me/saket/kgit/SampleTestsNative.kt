package me.saket.kgit

import kotlin.test.Test
import kotlin.test.assertTrue

class SampleTestsNative {
  @Test
  fun testHello() {
    assertTrue("macOS" in hello())
  }
}
