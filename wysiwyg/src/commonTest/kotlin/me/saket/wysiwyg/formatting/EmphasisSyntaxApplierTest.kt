package me.saket.wysiwyg.formatting

import kotlin.test.Test

class EmphasisSyntaxApplierTest : BaseMarkdownSyntaxApplierTest() {

  @Test fun `insert at cursor position`() {
    EmphasisSyntaxApplier.test(
        input = "You think ▮ can last?",
        output = "You think *▮* can last?"
    )
  }

  @Test fun `apply to selection`() {
    EmphasisSyntaxApplier.test(
        input = "You think ▮this▮ can last?",
        output = "You think *this*▮ can last?"
    )
  }
}
