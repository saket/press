//
//  Created by Saket Narayan on 4/15/20.
//  Copyright © 2020 Saket Narayan. All rights reserved.
//

import Combine
import SwiftUI
import shared

struct HomeView: View {
  @EnvironmentObject var theme: AppTheme
  @State var selectedNote: NoteId? = nil
  private let navigator = ObservableNavigator()

  /// The home presenter must be kept here instead of NoteListView to
  /// avoid creating a new presenter instance everytime the body is
  /// updated. Creating new presenters should be alright in theory
  /// because they don't maintain any local state, but sometimes they
  /// emit immediately on subscription and that's dangerous if it
  /// happens everytime the View is re-rendered.
  @Subscribable var presenter: HomePresenter

  var body: some View {
    let notesWidth = Dimensions.noteListWidth
    let editorWidth = Dimensions.editorWidth

    return NavigationView {
      Subscribe($presenter) { (model: HomeUiModel, effects) in
        Group {
          /// SwiftUI doesn't offer a way to skip animation when showing
          /// Views *for the first time* so skip laying the Views altogether
          /// until we have something to show.
          if (!model.notes.isEmpty) {
            NoteListView(model, selection: self.$selectedNote)
          }
        }
          .frame(minWidth: 224, idealWidth: notesWidth, maxWidth: 508, maxHeight: .infinity)
          .padding(.top, 1) // A non-zero padding automatically pushes it down the titlebar ¯\_(ツ)_/¯
          .onReceive(navigator.listen(ComposeNewNote.self)) { key in
            selectedNote = key.noteId
          }
      }

      ZStack {
        Color(theme.palette.window.editorBackgroundColor)
        if (selectedNote != nil) {
          EditorView(openMode: EditorOpenMode.ExistingNote(noteId: selectedNote!))
        }
      }
        .frame(minWidth: 350, idealWidth: editorWidth, maxWidth: .infinity, maxHeight: .infinity)
        .padding(.top, -Dimensions.windowTitleBarHeight)
    }
      .padding(.top, -Dimensions.windowTitleBarHeight)  // Would be nice to not hardcode this.
      .navigationViewStyle(DoubleColumnNavigationViewStyle())
      .frame(maxWidth: .infinity, minHeight: 300, maxHeight: .infinity)
  }

  init() {
    let presenterFactory = PressApp.component.resolve(HomePresenterFactory.self)!
    let presenter = presenterFactory.create(args_: HomePresenter.Args(
      includeBlankNotes: true,
      navigator: navigator
    ))
    self._presenter = .init(presenter)
  }
}
