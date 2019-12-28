package me.saket.wysiwyg.formatting

import kotlin.test.Test

class HeadingSyntaxApplierTest : BaseApplyMarkdownSyntaxTest() {

  @Test fun `insert at cursor position at the end of the first line in a paragraph`() {
    HeadingSyntaxApplier.test(
        input = """
                |Alfred: Shall you be taking the Batpod sir?▮
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                |Alfred: The Lamborghini then? Much more subtle.
                """.trimMargin(),
        expect = """
                |# Alfred: Shall you be taking the Batpod sir?▮
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                |Alfred: The Lamborghini then? Much more subtle.
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position at the end of a line in the middle of a paragraph`() {
    HeadingSyntaxApplier.test(
        input = """
                |Alfred: Shall you be taking the Batpod sir?
                |Batman/Bruce Wayne: In the middle of the day Alfred?▮
                |Alfred: The Lamborghini then? Much more subtle.
                """.trimMargin(),
        expect = """
                |Alfred: Shall you be taking the Batpod sir?
                |# Batman/Bruce Wayne: In the middle of the day Alfred?▮
                |Alfred: The Lamborghini then? Much more subtle.
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position at the end of the last line in a paragraph`() {
    HeadingSyntaxApplier.test(
        input = """
                |Alfred: Shall you be taking the Batpod sir?
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                |Alfred: The Lamborghini then? Much more subtle.▮
                """.trimMargin(),
        expect = """
                |Alfred: Shall you be taking the Batpod sir?
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                |# Alfred: The Lamborghini then? Much more subtle.▮
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in blank content`() {
    HeadingSyntaxApplier.test(
        input = """
                |▮
                """.trimMargin(),
        expect = """
                |# ▮
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in blank content with leading new line`() {
    HeadingSyntaxApplier.test(
        input = """
                |
                |▮
                """.trimMargin(),
        expect = """
                |
                |# ▮
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in the middle of a paragraph`() {
    HeadingSyntaxApplier.test(
        input = """
                |Tell your men they work ▮ for me now. This is my city.
                """.trimMargin(),
        expect = """
                |# Tell your men they work ▮ for me now. This is my city.
                """.trimMargin()
    )
  }

  @Test fun `apply to selection in the middle of a paragraph`() {
    HeadingSyntaxApplier.test(
        input = """
                |Tell your men they work for ▮me▮ now. This is my city.
                """.trimMargin(),
        expect = """
                |# Tell your men they work for ▮me▮ now. This is my city.
                """.trimMargin()
    )
  }

  @Test fun `apply to selection to a whole paragraph`() {
    HeadingSyntaxApplier.test(
        input = """
                |▮Tell your men they work for me now. This is my city.▮
                """.trimMargin(),
        expect = """
                |# ▮Tell your men they work for me now. This is my city.▮
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position on a new line`() {
    HeadingSyntaxApplier.test(
        input = """
                |Tell your men they work for me now. This is my city.
                |▮
                """.trimMargin(),
        expect = """
                |Tell your men they work for me now. This is my city.
                |# ▮
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position on a new line with leading spaces`() {
    HeadingSyntaxApplier.test(
        input = """
                |Tell your men they work for me now. This is my city.
                |  ▮
                """.trimMargin(),
        expect = """
                |Tell your men they work for me now. This is my city.
                |#  ▮
                """.trimMargin()
    )
  }

  @Test fun `apply to a paragraph that is already a heading`() {
    HeadingSyntaxApplier.test(
        input = """
                |# Tell your men they work for me now. This is my city.▮
                """.trimMargin(),
        expect = """
                |## Tell your men they work for me now. This is my city.▮
                """.trimMargin()
    )
    HeadingSyntaxApplier.test(
        input = """
                |## Tell your men they work for me now. This is my city.▮
                """.trimMargin(),
        expect = """
                |### Tell your men they work for me now. This is my city.▮
                """.trimMargin()
    )
  }
}
