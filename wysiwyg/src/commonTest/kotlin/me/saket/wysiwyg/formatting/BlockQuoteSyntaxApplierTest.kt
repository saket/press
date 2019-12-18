package me.saket.wysiwyg.formatting

import kotlin.test.Test

class BlockQuoteSyntaxApplierTest : BaseMarkdownSyntaxApplierTest() {

  @Test fun `insert at cursor position at the end of the first line in a paragraph`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |Alfred: Shall you be taking the Batpod sir?▮
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                |Alfred: The Lamborghini then? Much more subtle.
                """.trimMargin(),
        output = """
                |> Alfred: Shall you be taking the Batpod sir?▮
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                |Alfred: The Lamborghini then? Much more subtle.
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position at the end of a line in the middle of a paragraph`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |Alfred: Shall you be taking the Batpod sir?
                |Batman/Bruce Wayne: In the middle of the day Alfred?▮
                |Alfred: The Lamborghini then? Much more subtle.
                """.trimMargin(),
        output = """
                |Alfred: Shall you be taking the Batpod sir?
                |> Batman/Bruce Wayne: In the middle of the day Alfred?▮
                |Alfred: The Lamborghini then? Much more subtle.
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position at the end of the last line in a paragraph`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |Alfred: Shall you be taking the Batpod sir?
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                |Alfred: The Lamborghini then? Much more subtle.▮
                """.trimMargin(),
        output = """
                |Alfred: Shall you be taking the Batpod sir?
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                |> Alfred: The Lamborghini then? Much more subtle.▮
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in blank content`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |▮
                """.trimMargin(),
        output = """
                |> ▮
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in blank content with leading new line`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |
                |▮
                """.trimMargin(),
        output = """
                |
                |> ▮
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in the middle of a paragraph`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |Tell your men they work ▮ for me now. This is my city.
                """.trimMargin(),
        output = """
                |> Tell your men they work ▮ for me now. This is my city.
                """.trimMargin()
    )
  }

  @Test fun `apply to selection in the middle of a paragraph`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |Tell your men they work for ▮me▮ now. This is my city.
                """.trimMargin(),
        output = """
                |> Tell your men they work for ▮me▮ now. This is my city.
                """.trimMargin()
    )
  }

  @Test fun `apply to selection to a whole paragraph`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |▮Tell your men they work for me now. This is my city.▮
                """.trimMargin(),
        output = """
                |> ▮Tell your men they work for me now. This is my city.▮
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position on a new line`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |Tell your men they work for me now. This is my city.
                |▮
                """.trimMargin(),
        output = """
                |Tell your men they work for me now. This is my city.
                |> ▮
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position on a new line with leading spaces`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |Tell your men they work for me now. This is my city.
                |  ▮
                """.trimMargin(),
        output = """
                |Tell your men they work for me now. This is my city.
                |>  ▮
                """.trimMargin()
    )
  }

  @Test fun `apply to a paragraph that is already a block-quote`() {
    BlockQuoteSyntaxApplier.test(
        input = """
                |> Tell your men they work for me now. This is my city.▮
                """.trimMargin(),
        output = """
                |>> Tell your men they work for me now. This is my city.▮
                """.trimMargin()
    )
    BlockQuoteSyntaxApplier.test(
        input = """
                |>> Tell your men they work for me now. This is my city.▮
                """.trimMargin(),
        output = """
                |>>> Tell your men they work for me now. This is my city.▮
                """.trimMargin()
    )
  }
}
