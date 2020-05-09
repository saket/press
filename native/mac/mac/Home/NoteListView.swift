//
// Created by Saket Narayan on 5/4/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import SwiftUI
import shared
import Swinject

struct NoteListView: View {
  @Subscribable var presenter: HomePresenter
  @Binding var selectedNoteId: UuidUuid?

  var body: some View {
    Subscribe($presenter) { model, _ in
      List(selection: self.$selectedNoteId) {
        ForEach(model.notes) { note in
          NoteRowView(note: note).tag(note.noteUuid)
        }
      }
    }
  }

  init(selection: Binding<UuidUuid?>) {
    self._selectedNoteId = selection

    let presenterFactory = PressApp.component.resolve(HomePresenterFactory.self)!
    let args = HomePresenter.Args(includeEmptyNotes: true)
    self._presenter = .init(presenterFactory.create(args: args))
  }
}

// Needed by ForEach.
extension HomeUiModel.Note: Identifiable {
  public var id: Int64 {
    self.adapterId
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
    enclosingScrollView!.drawsBackground = false
  }
}
