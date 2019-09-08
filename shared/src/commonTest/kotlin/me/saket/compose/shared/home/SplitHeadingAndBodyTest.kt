package me.saket.compose.shared.home

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
}