package me.saket.wysiwyg.formatting

import kotlin.test.Test

class InlineCodeSyntaxApplierTest : BaseMarkdownSyntaxApplierTest() {

  @Test fun `insert at cursor position`() {
    InlineCodeSyntaxApplier.test(
        input = "This is the ▮ exchange. There's no money you can steal!",
        output = "This is the `▮` exchange. There's no money you can steal!"
    )
  }

  @Test fun `apply to selection`() {
    InlineCodeSyntaxApplier.test(
        input = "Really? Then, why are you ▮people▮ in here?",
        output = "Really? Then, why are you `people`▮ in here?"
    )
  }
}
