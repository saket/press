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
  @Subscribable var presenter: EditorPresenter
  @State var editorText: String = ""

  var body: some View {
    // TODO: Can the @State and this be combined?
    let editorTextChanges = listen($editorText) {
      self.presenter.dispatch(event: EditorEventNoteTextChanged(text: $0))
    }

    return Subscribe($presenter) { model, effects in
      ZStack(alignment: .topLeading) {
        MultiLineTextField(text: editorTextChanges, onSetup: { view in
          view.textColor = NSColor(self.theme.palette.textColorPrimary)
          view.isRichText = false
          view.applyStyle(EditorUiStyles().editor)
          view.setPaddings(horizontal: 25, vertical: 35)
        })

        // Hint text for the heading.
        Text(model.hintText ?? "")
          .style(EditorUiStyles().editor)
          .offset(x: 25, y: 35)
          .foregroundColor(self.theme.palette.textColorHint)
      }
        .onReceive(effects.updateNoteText()) {
          // todo: consume effect.newSelection.
          self.editorText = $0.newText
        }
        .onDisappear {
          self.presenter.saveEditorContentOnExit(content: self.editorText)
        }
    }.frame(maxWidth: 750)
  }

  init(openMode: EditorOpenMode) {
    let presenterFactory = PressApp.component.resolve(EditorPresenterFactory.self)!
    _presenter = .init(presenterFactory.create(args_: EditorPresenter.Args(openMode: openMode)))
  }

  func listen<T>(_ binding: Binding<T>, listener: @escaping (T) -> Void) -> Binding<T> {
    listener(binding.wrappedValue)  // Initial value.
    return Binding<T>(get: {
      binding.wrappedValue
    }, set: {
      binding.wrappedValue = $0
      listener($0)
    })
  }
}

extension Publisher {
  func updateNoteText() -> AnyPublisher<EditorUiEffect.UpdateNoteText, Never> {
    return ofType(EditorUiEffect.UpdateNoteText.self)
  }
}
