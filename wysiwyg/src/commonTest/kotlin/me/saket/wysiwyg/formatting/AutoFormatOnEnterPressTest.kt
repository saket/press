package me.saket.wysiwyg.formatting

import kotlin.test.Test

class AutoFormatOnEnterPressTest : BaseApplyMarkdownSyntaxTest() {

  @Test fun `enter after fenced code syntax`() {
    AutoFormatOnEnterPress.onEnterTest(
        input = """
                |Alfred: Shall you be taking the Batpod sir?
                |```▮
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                """.trimMargin(),
        output = """
                |Alfred: Shall you be taking the Batpod sir?
                |```
                |▮
                |```
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                """.trimMargin()
    )
  }

  @Test fun `enter after fenced code syntax and language name`() {
    AutoFormatOnEnterPress.onEnterTest(
        input = """
                |Alfred: Shall you be taking the Batpod sir?
                |```kotlin▮
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                """.trimMargin(),
        output = """
                |Alfred: Shall you be taking the Batpod sir?
                |```kotlin
                |▮
                |```
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                """.trimMargin()
    )
  }

  @Test fun `enter after fenced code syntax when already inside a fenced code block`() {
    AutoFormatOnEnterPress.onEnterTest(
        input = """
                |```
                |fun someCodeBlock() {}
                |```
                |
                |Alfred: Shall you be taking the Batpod sir?
                |
                |```kotlin▮
                |```
                |Batman/Bruce Wayne: In the middle of the day Alfred?
                |
                |```javaOmg
                |fun anotherCodeBlock() {}
                |```
                """.trimMargin(),
        output = null
    )
  }
}
