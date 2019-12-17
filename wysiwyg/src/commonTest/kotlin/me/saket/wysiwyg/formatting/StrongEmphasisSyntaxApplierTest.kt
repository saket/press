package me.saket.wysiwyg.formatting

import kotlin.test.Test

class StrongEmphasisSyntaxApplierTest : BaseMarkdownSyntaxApplierTest() {

  @Test fun `insert at cursor position`() {
    StrongEmphasisSyntaxApplier.test(
        input = "You don't ▮ these people anymore, you've given them everything.",
        output = "You don't **▮** these people anymore, you've given them everything."
    )
  }

  @Test fun `apply to selection`() {
    StrongEmphasisSyntaxApplier.test(
        input = "Not everything, ▮not yet▮.",
        output = "Not everything, **not yet**▮."
    )
  }
}
