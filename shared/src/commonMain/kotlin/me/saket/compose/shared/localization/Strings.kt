package me.saket.compose.shared;

import me.saket.compose.shared.Strings.Editor

data class Strings(val editor: Editor) {
  data class Editor(
    val newNoteHint: String
  )
}

val ENGLISH_STRINGS = Strings(
    editor = Editor(
        newNoteHint = "A wonderful note"
    )
)