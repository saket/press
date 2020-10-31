//
// Created by Saket Narayan on 5/7/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Swinject
import shared

class EditorComponent: Assembly {
  func assemble(container: Container) {
    container.register(EditorPresenterFactory.self) { r in
      class Factory : EditorPresenterFactory {
        func create(args__ args: EditorPresenter.Args) -> EditorPresenter {
          SharedEditorComponent.Companion().presenter(args: args)
        }
      }
      return Factory()
    }
  }
}
