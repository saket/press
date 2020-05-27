package me.saket.press.shared.home

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertEquals

class SplitHeadingAndBodyTest {

  @Test fun `text with h1 and body`() {
    val (heading, body) = SplitHeadingAndBody.split(
        """
        |# Level 1 heading
        |Body
        """.trimMargin()
    )
    assertEquals("Level 1 heading", heading)
    assertEquals("Body", body)
  }

  @Test fun `text with h1 with leading line break and body`() {
    val (heading, body) = SplitHeadingAndBody.split(
        """
        |
        |# Level 1 heading
        |Body
        """.trimMargin()
    )
    assertEquals("Level 1 heading", heading)
    assertEquals("Body", body)
  }

  @Test fun `text with h1 and body separated by multiple line breaks`() {
    val (heading, body) = SplitHeadingAndBody.split(
        """
        |# Level 1 heading
        |
        |
        |Body
        """.trimMargin()
    )
    assertEquals("Level 1 heading", heading)
    assertEquals("Body", body)
  }

  @Test fun `text with h6 and body`() {
    val (heading, body) = SplitHeadingAndBody.split(
        """
        |###### Level 6 heading
        |Body
        """.trimMargin()
    )
    assertEquals("Level 6 heading", heading)
    assertEquals("Body", body)
  }

  @Test fun `text with heading only`() {
    val (heading, body) = SplitHeadingAndBody.split(
        """
        |# Heading
        """.trimMargin()
    )
    assertEquals("Heading", heading)
    assertEquals("", body)
  }

  @Test fun `text with body only`() {
    val (heading, body) = SplitHeadingAndBody.split(
        """
        |Body
        """.trimMargin()
    )
    assertEquals("", heading)
    assertEquals("Body", body)
  }

  @Test fun `text with empty heading`() {
    val (heading, body) = SplitHeadingAndBody.split(
        """
        |#
        |#
        | 
        """.trimMargin()
    )
    assertEquals("", heading)
    assertEquals("#\n#\n ", body)
  }

  @Test fun `text with heading without any space after #`() {
    val (heading, body) = SplitHeadingAndBody.split(
        """
        |#Heading
        |Body
        """.trimMargin()
    )
    assertEquals("", heading)
    assertEquals("#Heading\nBody", body)
  }

  @Test fun `text with trailing space in the heading`() {
    val (heading1, body1) = SplitHeadingAndBody.split("# Heading ")
    assertThat(heading1).isEqualTo("Heading")
    assertThat(body1).isEmpty()

    val (heading2, body2) = SplitHeadingAndBody.split("# Heading \nBody")
    assertThat(heading2).isEqualTo("Heading")
    assertThat(body2).isEqualTo("Body")
  }
}
