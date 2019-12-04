package me.saket.press.shared.home

import me.saket.press.shared.editor.EditorEvent

interface HomeEvent {
  object NewNoteClicked : HomeEvent
}

inline class WindowFocusChanged(val hasFocus: Boolean) : EditorEvent
