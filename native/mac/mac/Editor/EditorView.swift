//
// Created by Saket Narayan on 5/4/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Combine
import shared
import SwiftUI

struct EditorView: View {
  @EnvironmentObject var theme: AppTheme
  private let style = EditorUiStyles().editor

  private let openMode: EditorOpenMode
  @Subscribable var presenter: EditorPresenter
  @ObservedObject var editorText: Listenable<String>

  var body: some View {
    return Subscribe($presenter) { (model: EditorUiModel, effects) in
      ZStack(alignment: .topLeading) {
        // Hint text for the heading.
        if (model.hintText != nil) {
          Text(model.hintText!)
            .style(self.style)
            .offset(x: 25, y: 35)
            .foregroundColor(self.theme.palette.textColorHint)
        }

        MultiLineTextField(text: self.$editorText.value, onSetup: { view in
          view.textColor = NSColor(self.theme.palette.textColorPrimary)
          view.isRichText = false
          view.applyStyle(self.style)
          view.setPaddings(horizontal: 25, vertical: 35)
        })
      }
        .onReceive(effects.updateNoteText()) {
          // TODO: consume effect.newSelection.
          self.editorText.value = $0.newText
        }
        .onDisappear {
          /// TODO: this is dangerous. Saving the editor content before it's
          /// populated from the DB will cause it to get overridden.
          self.presenter.saveEditorContentOnExit(content: self.editorText.value)
        }
    }
      .frame(maxWidth: 750)
      /// This is extremely important. If the same View is used for showing
      /// different types of data (different notes in this case), SwiftUI
      /// needs to be able to distinguish them in order to give them _separate
      /// lifecycle_.
      .id(openMode)
  }

  init(openMode: EditorOpenMode) {
    self.openMode = openMode

    let factory = PressApp.component.resolve(EditorPresenterFactory.self)!
    let presenter = factory.create(args_: EditorPresenter.Args(openMode: openMode))

    self._presenter = .init(presenter)
    self.editorText = Listenable(initial: "") {
      presenter.dispatch(event: EditorEventNoteTextChanged(text: $0))
    }
  }
}

extension Publisher {
  func updateNoteText() -> AnyPublisher<EditorUiEffect.UpdateNoteText, Never> {
    return ofType(EditorUiEffect.UpdateNoteText.self)
  }
}
