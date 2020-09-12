package me.saket.press.shared.note

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertEquals

class HeadingAndBodyParserTest {

  @Test fun `text with h1 and body`() {
    val (heading, body) = HeadingAndBody.parse(
        """
        |# Level 1 heading
        |Body
        """.trimMargin()
    )
    assertEquals("Level 1 heading", heading)
    assertEquals("Body", body)
  }

  @Test fun `text with h1 with leading line break and body`() {
    val (heading, body) = HeadingAndBody.parse(
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
    val (heading, body) = HeadingAndBody.parse(
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
    val (heading, body) = HeadingAndBody.parse(
        """
        |###### Level 6 heading
        |Body
        """.trimMargin()
    )
    assertEquals("Level 6 heading", heading)
    assertEquals("Body", body)
  }

  @Test fun `text with heading only`() {
    val (heading, body) = HeadingAndBody.parse(
        """
        |# Heading
        """.trimMargin()
    )
    assertEquals("Heading", heading)
    assertEquals("", body)
  }

  @Test fun `text with body only`() {
    val (heading, body) = HeadingAndBody.parse(
        """
        |Body
        """.trimMargin()
    )
    assertEquals("", heading)
    assertEquals("Body", body)
  }

  @Test fun `text with empty heading`() {
    val (heading, body) = HeadingAndBody.parse(
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
    val (heading, body) = HeadingAndBody.parse(
        """
        |#Heading
        |Body
        """.trimMargin()
    )
    assertEquals("", heading)
    assertEquals("#Heading\nBody", body)
  }

  @Test fun `text with trailing space in the heading`() {
    val (heading1, body1) = HeadingAndBody.parse("# Heading ")
    assertThat(heading1).isEqualTo("Heading")
    assertThat(body1).isEmpty()

    val (heading2, body2) = HeadingAndBody.parse("# Heading \nBody")
    assertThat(heading2).isEqualTo("Heading")
    assertThat(body2).isEqualTo("Body")
  }

  @Test fun `parse with spacings`() {
    with(HeadingAndBody.parse(" ##  Heading \n\n  Body ", trimSpacings = false)) {
      assertThat(heading).isEqualTo("Heading ")
      assertThat(body).isEqualTo("\n  Body ")
      assertThat(headingSyntax).isEqualTo(" ##  ")
    }

    with(HeadingAndBody.parse("# Heading only", trimSpacings = false)) {
      assertThat(heading).isEqualTo("Heading only")
      assertThat(body).isEqualTo("")
      assertThat(headingSyntax).isEqualTo("# ")
    }

    with(HeadingAndBody.parse(" Body\nonly ", trimSpacings = false)) {
      assertThat(heading).isEqualTo("")
      assertThat(body).isEqualTo(" Body\nonly ")
      assertThat(headingSyntax).isEqualTo("")
    }
  }

  @Test fun `read heading syntax`() {
    val parse = { content: String -> HeadingAndBody.parse(content, trimSpacings = false).headingSyntax }

    assertThat(parse("# Heading1")).isEqualTo("# ")
    assertThat(parse("## Heading2")).isEqualTo("## ")
    assertThat(parse("### Heading\nwith body")).isEqualTo("### ")
    assertThat(parse("Body without heading")).isEqualTo("")
  }

  @Test fun `prefix heading`() {
    val prefix = { content: String -> HeadingAndBody.prefixHeading(content, prefix = "Conflicted: ") }

    assertThat(prefix("# Heading\nBody")).isEqualTo("# Conflicted: Heading\nBody")
    assertThat(prefix("# Heading only")).isEqualTo("# Conflicted: Heading only")
    assertThat(prefix("Body only")).isEqualTo("Conflicted: Body only")
    assertThat(prefix(" ##  Heading\nBody\nwith weird spacings"))
        .isEqualTo(" ##  Conflicted: Heading\nBody\nwith weird spacings")
  }
}
