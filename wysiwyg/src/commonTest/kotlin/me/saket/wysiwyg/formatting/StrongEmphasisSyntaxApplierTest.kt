package me.saket.wysiwyg.formatting

import kotlin.test.Test

class StrongEmphasisSyntaxApplierTest : BaseApplyMarkdownSyntaxTest() {

  @Test fun `insert at cursor position`() {
    StrongEmphasisSyntaxApplier.test(
        input = "You don't ▮ these people anymore, you've given them everything.",
        expect ="You don't **▮** these people anymore, you've given them everything."
    )
  }

  @Test fun `apply to selection`() {
    StrongEmphasisSyntaxApplier.test(
        input = "Not everything, ▮not yet▮.",
        expect ="Not everything, ▮**not yet**▮."
    )
  }
}
