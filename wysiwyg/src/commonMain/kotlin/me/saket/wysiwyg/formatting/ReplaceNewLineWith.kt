package me.saket.wysiwyg.formatting

sealed class ReplaceNewLineWith {

  data class InsertLetters(
    val replacement: CharSequence,
    val newSelection: TextSelection
  ) : ReplaceNewLineWith()

  /**
   * Equivalent of sending backspace events to the text field.
   * @param deleteCount Number of letters to delete from the cursor position.
   */
  data class DeleteLetters(
    val deleteCount: Int
  ) : ReplaceNewLineWith()
}
