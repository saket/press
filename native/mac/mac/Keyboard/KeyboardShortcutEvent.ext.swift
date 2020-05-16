//
// Created by Saket Narayan on 5/16/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Cocoa
import Foundation
import shared

extension KeyboardShortcutEvent {
  static func from(shortcut: NSMenuItem) -> KeyboardShortcutEvent {
    let modifiersMask = shortcut.keyEquivalentModifierMask

    var modifiers = [KeyboardShortcutEvent.Key]()
    if (modifiersMask.contains(NSEvent.ModifierFlags.command)) {
      modifiers.append(KeyboardShortcutEvent.Key.cmd)
    }
    return KeyboardShortcutEvent(modifiers: modifiers, character: shortcut.keyEquivalent)
  }
}
