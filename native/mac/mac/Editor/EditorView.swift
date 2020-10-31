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
  @ObservedObject var editorText: Listenable<String>
  private let openMode: EditorOpenMode

  var body: some View {
    return Subscribe($presenter) { (model: EditorUiModel, effects) in
      ZStack(alignment: .topLeading) {
        // Hint text for the heading.
        if (model.hintText != nil) {
          Text(model.hintText!)
            .style(TextStyles().mainBody)
            .offset(x: 25, y: 35)
            .foregroundColor(self.theme.palette.textColorHint)
        }

        MultiLineTextField(text: self.$editorText.value, onSetup: { view in
          view.textColor = NSColor(self.theme.palette.textColorPrimary)
          view.isRichText = false
          view.applyStyle(TextStyles().mainBody)
          view.setPaddings(horizontal: 25, vertical: 35)
        })
      }
        .onReceive(effects.updateNoteText()) {
          // TODO: consume effect.newSelection.
          self.editorText.value = $0.newText
        }
        // TODO: consume BlockedDueToSyncConflict
        .onDisappear {
          /// TODO: this is dangerous. Saving the editor content before it's
          /// populated from the DB will cause it to get overridden.
          self.presenter.saveEditorContentOnClose(content: self.editorText.value)
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

    class Nav : Navigator {
      func lfg(screen: ScreenKey) {
        // TODO
      }
    }

    let factory = PressApp.component.resolve(EditorPresenterFactory.self)!
    let args = EditorPresenter.Args(openMode: openMode, deleteBlankNewNoteOnExit: false, navigator: Nav())
    let presenter = factory.create(args__: args)

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
