package me.saket.wysiwyg.formatting

import kotlin.test.Test

class EmphasisSyntaxApplierTest : BaseApplyMarkdownSyntaxTest() {

  @Test fun `insert at cursor position`() {
    EmphasisSyntaxApplier.test(
        input = "You think ▮ can last?",
        expect ="You think *▮* can last?"
    )
  }

  @Test fun `apply to selection`() {
    EmphasisSyntaxApplier.test(
        input = "You think ▮this▮ can last?",
        expect ="You think ▮*this*▮ can last?"
    )
  }
}
