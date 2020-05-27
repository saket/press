package me.saket.press.shared.keyboard

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.subject.publish.PublishSubject
import me.saket.press.shared.keyboard.KeyboardShortcutEvent.Key.CMD
import me.saket.press.shared.keyboard.KeyboardShortcutEvent.Key.N

/**
 * Event bus for letting presenters respond to keyboard shortcuts.
 */
interface KeyboardShortcuts {
  companion object {
    val newNote = KeyboardShortcutEvent(CMD, N)
  }

  fun broadcast(event: KeyboardShortcutEvent)
  fun listen(shortcut: KeyboardShortcutEvent): Observable<KeyboardShortcutEvent>
}

class RealKeyboardShortcuts : KeyboardShortcuts {
  private val events = PublishSubject<KeyboardShortcutEvent>()

  override fun broadcast(event: KeyboardShortcutEvent) =
    events.onNext(event)

  override fun listen(shortcut: KeyboardShortcutEvent) =
    events.filter { it == shortcut }
}
