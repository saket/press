package me.saket.wysiwyg.formatting

import kotlin.test.Test

class FencedCodeBlockSyntaxApplierTest : BaseApplyMarkdownSyntaxTest() {

  @Test fun `insert at cursor position at the end of the first line in a paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |Gordon: What's your name son?▮
                |John Blake: Blake sir.
                |Gordon: You have something you wanna ask me Officer Blake?
                """.trimMargin(),
        expect ="""
                |```
                |Gordon: What's your name son?▮
                |```
                |John Blake: Blake sir.
                |Gordon: You have something you wanna ask me Officer Blake?
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position at the end of a line in the middle of a paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |Gordon: What's your name son?
                |John Blake: Blake sir.▮
                |Gordon: You have something you wanna ask me Officer Blake?
                """.trimMargin(),
        expect ="""
                |Gordon: What's your name son?
                |```
                |John Blake: Blake sir.▮
                |```
                |Gordon: You have something you wanna ask me Officer Blake?
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position at the end of the last line in a paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |Gordon: What's your name son?
                |John Blake: Blake sir.
                |Gordon: You have something you wanna ask me Officer Blake?▮
                """.trimMargin(),
        expect ="""
                |Gordon: What's your name son?
                |John Blake: Blake sir.
                |```
                |Gordon: You have something you wanna ask me Officer Blake?▮
                |```
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in blank content`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |▮
                """.trimMargin(),
        expect ="""
                |```
                |▮
                |```
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in blank content with leading new line`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |
                |▮
                """.trimMargin(),
        expect ="""
                |
                |```
                |▮
                |```
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position in the middle of a paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |You have something you ▮ wanna ask me Officer Blake?
                """.trimMargin(),
        expect ="""
                |```
                |You have something you ▮ wanna ask me Officer Blake?
                |```
                """.trimMargin()
    )
  }

  @Test fun `apply to selection in the middle of a paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |You have something you wanna ▮ask me▮ Officer Blake?
                """.trimMargin(),
        expect ="""
                |```
                |You have something you wanna ▮ask me▮ Officer Blake?
                |```
                """.trimMargin()
    )
  }

  @Test fun `apply to selection to a whole paragraph`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |▮You have something you wanna ask me Officer Blake?▮
                """.trimMargin(),
        expect ="""
                |```
                |▮You have something you wanna ask me Officer Blake?▮
                |```
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position on a new line`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |What's your name son?
                |▮
                """.trimMargin(),
        expect ="""
                |What's your name son?
                |```
                |▮
                |```
                """.trimMargin()
    )
  }

  @Test fun `insert at cursor position on a new line with leading spaces`() {
    FencedCodeBlockSyntaxApplier.test(
        input = """
                |What's your name son?
                |  ▮
                """.trimMargin(),
        expect ="""
                |What's your name son?
                |```
                |  ▮
                |```
                """.trimMargin()
    )
  }
}
