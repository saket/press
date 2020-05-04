//
//  ContentView.swift
//  mac
//
//  Created by Saket Narayan on 4/15/20.
//  Copyright © 2020 Saket Narayan. All rights reserved.
//

import Combine
import SwiftUI
import shared

struct HomeView: View {
  let presenter: HomePresenter
  @EnvironmentObject var theme: AppTheme

  var body: some View {
    Subscribe(presenter) { model, _ in
      List {
        ForEach(model.notes) { note in
          NoteRowView(note: note)
        }
      }
    }
      .padding(.top, 8) // Space to let the user drag the window because the title is hidden.
      .frame(maxWidth: .infinity, maxHeight: .infinity)
  }

  init(presenterFactory: HomePresenterFactory) {
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

// TODO(saket): can this be made to work by creating a fake presenter?
//struct HomeView_Previews: PreviewProvider {
//  static var previews: some View {
//    HomeView()
//  }
//}
