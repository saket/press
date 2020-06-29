package me.saket.press.shared.localization

import me.saket.press.shared.localization.Strings.Common
import me.saket.press.shared.localization.Strings.Editor
import me.saket.press.shared.localization.Strings.Home
import me.saket.press.shared.localization.Strings.SyncPreferences

data class Strings(
  val common: Common,
  val home: Home,
  val editor: Editor,
  val syncPreferences: SyncPreferences
) {
  data class Common(
    val closeNavIconDescription: String,
    val genericError: String,
    val retry: String
  )

  data class Home(
    val preferences: String
  )

  data class Editor(
    val newNoteHints: List<String>,
    val openUrl: String,
    val editUrl: String
  )

  data class SyncPreferences(
    val title: String
  )
}

val ENGLISH_STRINGS = Strings(
    common = Common(
        closeNavIconDescription = "Go back",
        genericError = "Something went wrong, try again?",
        retry = "Retry"
    ),
    home = Home(
        preferences = "Preferences"
    ),
    editor = Editor(
        newNoteHints = listOf(
            "A wonderful note",
            "It begins with a word",
            "This is the beginning",
            "Once upon a time",
            "Unleash those wild ideas",
            "Untitled composition",
            "Here we go",
            "Type your heart out"
        ),
        openUrl = "Open",
        editUrl = "Edit"
    ),
    syncPreferences = SyncPreferences(
        title = "Sync"
    )
)
