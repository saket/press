package me.saket.wysiwyg.formatting

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ParagraphBoundsTest : BaseTextSelectionTest() {

  @Test fun `empty paragraph`() {
    val (text, selection) = decodeSelection(
      text = """
             |▮
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 0, endExclusive = 0))
  }

  @Test fun `cursor after a whitespace`() {
    val (text, selection) = decodeSelection(
      text = """
             | ▮
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 0, endExclusive = 1))
  }

  @Test fun `cursor surrounded by whitespaces`() {
    val (text, selection) = decodeSelection(
      text = """
             | ▮ 
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)

    // Note to future self: if you're wondering why end=1,
    // it's because there are invisible whitespaces in the text.
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 0, endExclusive = 2))
  }

  @Test fun `empty paragraph with a leading empty line`() {
    val (text, selection) = decodeSelection(
      text = """
             |
             |▮
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 1, endExclusive = 1))
  }

  @Test fun `empty paragraph with a multiple leading empty lines`() {
    val (text, selection) = decodeSelection(
      text = """
             |
             |
             |
             |
             |▮
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 4, endExclusive = 4))
  }

  @Test fun `empty paragraph with a leading paragraph`() {
    val (text, selection) = decodeSelection(
      text = """
             |Alfred: Shall you be taking the Batpod sir?
             |▮
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 44, endExclusive = 44))
  }

  @Test fun `empty paragraph with a following empty line`() {
    val (text, selection) = decodeSelection(
      text = """
             |▮
             |
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 0, endExclusive = 0))
  }

  @Test fun `blank paragraph after a blank line`() {
    val (text, selection) = decodeSelection(
      text = """
             |  
             | ▮
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 3, endExclusive = 4))
  }

  @Test fun `blank paragraph surrounded by blank lines`() {
    val (text, selection) = decodeSelection(
      text = """
             |  
             | ▮
             | 
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 3, endExclusive = 4))
  }

  @Test fun `cursor at the starting of a single paragraph`() {
    val (text, selection) = decodeSelection(
      text = """
             |▮Alfred: Shall you be taking the Batpod sir?
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 0, endExclusive = 43))
  }

  @Test fun `cursor in the middle of a single paragraph`() {
    val (text, selection) = decodeSelection(
      text = """
             |Alfred: Shall you be ▮taking the Batpod sir?
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 0, endExclusive = 43))
  }

  @Test fun `cursor at the end of a single paragraph`() {
    val (text, selection) = decodeSelection(
      text = """
             |Alfred: Shall you be taking the Batpod sir?▮
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 0, endExclusive = 43))
  }

  @Test fun `cursor at the end of a paragraph surrounded by paragraphs`() {
    val (text, selection) = decodeSelection(
      text = """
             |Alfred: Shall you be taking the Batpod sir?
             |
             |Batman/Bruce Wayne: In the middle of the day Alfred?▮
             |
             |Alfred: The Lamborghini then? Much more subtle.
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 45, endExclusive = 97))
  }

  @Test fun `cursor at the starting of a paragraph surrounded by paragraphs`() {
    val (text, selection) = decodeSelection(
      text = """
             |Alfred: Shall you be taking the Batpod sir?
             |
             |▮Batman/Bruce Wayne: In the middle of the day Alfred?
             |
             |Alfred: The Lamborghini then? Much more subtle.
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 45, endExclusive = 97))
  }

  @Test fun `cursor in the middle of a paragraph surrounded by paragraphs`() {
    val (text, selection) = decodeSelection(
      text = """
             |Alfred: Shall you be taking the Batpod sir?
             |
             |Batman/Bruce Wayne: In ▮the middle of the day Alfred?
             |
             |Alfred: The Lamborghini then? Much more subtle.
             """.trimMargin()
    )
    val bounds = ParagraphBounds.find(text, selection)
    assertThat(bounds).isEqualTo(ParagraphBounds(start = 45, endExclusive = 97))
  }
}
