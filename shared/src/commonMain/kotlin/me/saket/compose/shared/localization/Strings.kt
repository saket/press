package me.saket.compose.shared.localization

import me.saket.compose.shared.localization.Strings.Editor

data class Strings(val editor: Editor) {
  data class Editor(
    val newNoteHints: List<String>
  )
}

val ENGLISH_STRINGS = Strings(
    editor = Editor(
        newNoteHints = listOf(
            "A wonderful note",
            "It beings with a word",
            "This is the beginning",
            "Once upon a time",
            "Unleash those wild ideas",
            "Untitled composition",
            "Here we go",
            "Type your heart out"
        )
    )
)