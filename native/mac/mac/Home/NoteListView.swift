//
// Created by Saket Narayan on 5/4/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import SwiftUI
import shared
import Swinject
import Combine

struct NoteListView: View {
  @EnvironmentObject var theme: AppTheme
  @Binding var selection: NoteId?
  let model: HomeUiModel

  var body: some View {
    VStack(alignment: .leading) {
      List(selection: self.$selection) {
        ForEach(model.notes, id: \.adapterId) { (note: HomeUiModel.Note) in
          NoteRowView(note: note)
            .tag(note.noteId)
            .background(self.listSelectionColor(note))
        }.removeListMargins()
      }
    }
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

// List on macOS uses an opaque background with no option for
// removing/changing it. listRowBackground() doesn't work either.
// This workaround works because List is backed by NSTableView
// on macOS.
extension NSTableView {
  open override func viewDidMoveToWindow() {
    super.viewDidMoveToWindow()

    backgroundColor = .clear
    enclosingScrollView?.drawsBackground = false

    /// SwiftUI doesn't offer any way to set highlight
    /// colors so we draw them manually instead.
    selectionHighlightStyle = .none
  }
}

extension View {
  /// SwiftUI has additional spacing around list items on macOS
  /// with option for removing them. Using negative margins until
  /// this is fixed.
  func removeListMargins() -> some View {
    return padding(.horizontal, -8).padding(.vertical, -4)
  }
}
