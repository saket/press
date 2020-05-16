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

  var body: some View {
    let notesWidth = Dimensions.noteListWidth
    let editorWidth = Dimensions.editorWidth

    return NavigationView {
      NoteListView(selection: self.$selectedNote)
        .frame(minWidth: 224, idealWidth: notesWidth, maxWidth: 508, maxHeight: .infinity)
        .padding(.top, 1) // A non-zero padding automatically pushes it down the titlebar ¯\_(ツ)_/¯

      ZStack {
        Color(self.theme.palette.window.editorBackgroundColor)

        if (self.selectedNote != nil) {
          EditorView(openMode: EditorOpenMode.ExistingNote(noteId: self.selectedNote!))
        }
      }
        .frame(minWidth: 350, idealWidth: editorWidth, maxWidth: .infinity, maxHeight: .infinity)
        .padding(.top, -Dimensions.windowTitleBarHeight)
    }
      .padding(.top, -Dimensions.windowTitleBarHeight)  // Would be nice to not hardcode this.
      .navigationViewStyle(DoubleColumnNavigationViewStyle())
      .frame(maxWidth: .infinity, maxHeight: .infinity)
  }
}


// TODO(saket): can this be made to work by creating a fake presenter?
//struct HomeView_Previews: PreviewProvider {
//  static var previews: some View {
//    HomeView()
//  }
//}
