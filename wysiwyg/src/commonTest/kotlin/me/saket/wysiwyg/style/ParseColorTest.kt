package me.saket.wysiwyg.style

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertFails

class ParseColorTest {

  @Test fun `parsing of valid color`() {
    val validTestData = mapOf(
      "#FFFFFF" to 0xFFFFFFFF.toInt(),
      "#000000" to 0xFF000000.toInt(),
      "#2F323F" to 0xFF2F323F.toInt(),
      "#AAC6D1FF" to 0xAAC6D1FF.toInt()
    )

    validTestData.forEach { (colorHex, expectedColorInt) ->
      assertThat(colorHex.parseColor(), name = colorHex).isEqualTo(expectedColorInt)
    }

    assertFails {
      "FFFFFF".parseColor()
    }
  }
}
