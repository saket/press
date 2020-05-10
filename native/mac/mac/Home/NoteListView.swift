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
        ForEach(model.notes, id: \.adapterId) { (note: HomeUiModel.Note) in
        }.removeListMargins()
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

// List on macOS uses an opaque background with no option for
// removing/changing it. listRowBackground() doesn't work either.
// This workaround works because List is backed by NSTableView
// on macOS.
extension NSTableView {
  open override func viewDidMoveToWindow() {
    super.viewDidMoveToWindow()

    backgroundColor = .clear
    enclosingScrollView?.drawsBackground = false

extension View {
  /// SwiftUI has additional spacing around list items on macOS
  /// with option for removing them. Using negative margins until
  /// this is fixed.
  func removeListMargins() -> some View {
    return padding(.horizontal, -8).padding(.vertical, -4)
  }
}
