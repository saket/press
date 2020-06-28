//
// Created by Saket Narayan on 5/16/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Swinject
import shared

class KeyboardShortcutsComponent: Assembly {
  func assemble(container: Container) {
    container.register(KeyboardShortcuts.self) { _ in
      SharedAppComponent.Companion().keyboardShortcuts()
    }
  }
}
