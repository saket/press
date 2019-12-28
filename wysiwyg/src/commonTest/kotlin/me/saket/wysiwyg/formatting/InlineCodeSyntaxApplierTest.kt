package me.saket.wysiwyg.formatting

import kotlin.test.Test

class InlineCodeSyntaxApplierTest : BaseApplyMarkdownSyntaxTest() {

  @Test fun `insert at cursor position`() {
    InlineCodeSyntaxApplier.test(
        input = "This is the ▮ exchange. There's no money you can steal!",
        expect = "This is the `▮` exchange. There's no money you can steal!"
    )
  }

  @Test fun `apply to selection`() {
    InlineCodeSyntaxApplier.test(
        input = "Really? Then, why are you ▮people▮ in here?",
        expect = "Really? Then, why are you ▮`people`▮ in here?"
    )
  }
}
