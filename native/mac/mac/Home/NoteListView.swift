//
// Created by Saket Narayan on 5/4/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import SwiftUI
import shared
import Swinject
import Combine
import Introspect

struct NoteListView: View {
  @EnvironmentObject var theme: AppTheme
  @Binding var selection: NoteId?
  let model: HomeUiModel

  var body: some View {
    List(selection: self.$selection) {
      ForEach(model.notes, id: \.adapterId) { (note: HomeUiModel.Note) in
        NoteRowView(note: note)
          .tag(note.noteId)
          .background(self.listSelectionColor(note))
      }
        .removeListMargins()
        .animation(nil) // Avoid animating content (including background) changes.
    }
      .removeBackground()
      .animation(.easeIn, value: model)
  }

  init(_ model: HomeUiModel, selection: Binding<NoteId?>) {
    self.model = model
    self._selection = selection
  }

  func listSelectionColor(_ note: HomeUiModel.Note) -> Color {
    if (selection == note.noteId) {
      return Color(theme.palette.window.editorBackgroundColor)
    } else {
      return Color.clear
    }
  }
}

extension Publisher {
  func composeNewNote() -> AnyPublisher<HomeUiEffect.ComposeNewNote, Never> {
    return ofType(HomeUiEffect.ComposeNewNote.self)
  }
}

