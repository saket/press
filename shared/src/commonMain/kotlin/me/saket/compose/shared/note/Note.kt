package me.saket.compose.shared.note

import kotlin.DeprecationLevel.ERROR

@Deprecated(
    message = "foo",
    replaceWith = ReplaceWith("Note", imports = ["me.saket.compose.data.shared.Note"]),
    level = ERROR
)
data class Note(
  val id: Long,
  val title: String,
  val body: String
)