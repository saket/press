//
//  Created by Saket Narayan on 4/15/20.
//  Copyright © 2020 Saket Narayan. All rights reserved.
//

import Combine
import SwiftUI
import shared

struct HomeView: View {
  @EnvironmentObject var theme: AppTheme
  private let notesWidth = Dimensions.noteListWidth
  private let editorWidth = Dimensions.editorWidth

  var body: some View {
    NavigationView {
      NoteListView()
        .frame(minWidth: 224, idealWidth: notesWidth, maxWidth: 508, maxHeight: .infinity)
        .padding(.top, 1) // A non-zero padding automatically pushes it down the titlebar ¯\_(ツ)_/¯

      EditorView()
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
