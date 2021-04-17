@file:Suppress("IllegalIdentifier")

package me.saket.wysiwyg

import org.junit.Test

class WysiwygTest {

  @Test fun `spans should be correctly created`() {
    // TODO.
  }

  companion object {
    val MARKDOWN = """
      |**Bold text**
      |*Italic text*
      |[Link](https://url.com)
      |~~Strikethrough~~
      |`Inline code`
      |
      |    Indented code block
      |    
      |```
      |Fenced code block
      |```
      |
      |> Block quote
      |
      |Ordered list block
      |1. Item A
      |2. Item B
      |
      |Unordered list block
      |- Item A
      |- Item B
      |
      |---
      |***
      |___
      |
      |# Heading 1
      |## Heading 2
      |### Heading 3
      |#### Heading 4
      |##### Heading 5
      |###### Heading 6
    """.trimMargin()
  }
}
