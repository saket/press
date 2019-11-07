package me.saket.press.shared.editor

interface EditorEvent {
  data class NoteTextChanged(val text: String) : EditorEvent
}