package me.saket.press.shared.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class HighlightedTextTest {
  @Test fun `search text is the first word`() {
    val highlighted = "Press is a wysiwyg writer for crafting notes".highlightInNoteBody(searchText = "press")
    assertThat(highlighted).isEqualTo(
      "▮Press▮ is a wysiwyg writer for crafting notes".debugHighlight()
    )
  }

  @Test fun `search text is in the only word`() {
    val highlighted = "Pressisawysiwygwriterforcraftingnotes".highlightInNoteBody(searchText = "wysiwyg")
    assertThat(highlighted).isEqualTo(
      "Pressisa▮wysiwyg▮writerforcraftingnotes".debugHighlight()
    )
  }

  @Test fun `search text is the second word`() {
    val highlighted = "Press is a wysiwyg writer for crafting notes".highlightInNoteBody(searchText = "is")
    assertThat(highlighted).isEqualTo(
      "Press ▮is▮ a wysiwyg writer for crafting notes".debugHighlight()
    )
  }

  @Test fun `search text is after the second word`() {
    val highlighted = "Press is a wysiwyg writer for crafting notes".highlightInNoteBody(searchText = "ter for")
    assertThat(highlighted).isEqualTo(
      "... wysiwyg wri▮ter for▮ crafting notes".debugHighlight()
    )
  }

  @Test fun `search text is after the second word with lots of spaces`() {
    val highlighted = "Press is a wysiwyg           writer for crafting notes".highlightInNoteBody(searchText = "ter for")
    assertThat(highlighted).isEqualTo(
      "... wysiwyg wri▮ter for▮ crafting notes".debugHighlight()
    )
  }

  @Test fun `search text is the last word`() {
    val highlighted = "Press is a wysiwyg writer for crafting notes".highlightInNoteBody(searchText = "notes")
    assertThat(highlighted).isEqualTo(
      "... crafting ▮notes▮".debugHighlight()
    )
  }

  @Test fun `search term does not exist in a string`() {
    val highlighted = "Press is a wysiwyg writer for crafting notes".highlightInNoteBody(searchText = "nicolas cage")
    assertThat(highlighted).isEqualTo(HighlightedText("Press is a wysiwyg writer for crafting notes"))
  }

  @Test fun `trim spacings`() {
    val highlighted = """
      |- Mangoes   and Apples
      |- Milk
      |-   Sausages
      """.trimMargin()
    assertThat(highlighted.highlightInNoteBody(searchText = "")).isEqualTo(
      HighlightedText("- Mangoes and Apples - Milk - Sausages")
    )
  }

  private fun String.debugHighlight(): HighlightedText {
    val text: String = this
    require('▮' in text)
    return HighlightedText(
      text = text.replace("▮", ""),
      highlight = IntRange(
        start = text.indexOfFirst { it == '▮' },
        endInclusive = text.indexOfLast { it == '▮' } - 1
      )
    )
  }
}
