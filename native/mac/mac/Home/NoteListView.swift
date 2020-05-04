//
// Created by Saket Narayan on 5/4/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import SwiftUI
import shared
import Swinject

struct NoteListView: View {
  private let presenter: HomePresenter

  var body: some View {
    Subscribe(presenter) { model, _ in
      List {
        ForEach(model.notes) { note in
          NoteRowView(note: note)
        }
      }
    }
  }

  init() {
    let presenterFactory = AppDelegate.component.resolve(HomePresenterFactory.self)!
    let args = HomePresenter.Args(includeEmptyNotes: true)
    presenter = presenterFactory.create(args: args)
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
