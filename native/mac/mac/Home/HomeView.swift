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
  let presenterFactory: HomePresenterFactory
  @EnvironmentObject var theme: AppTheme

  var body: some View {
    NavigationView {
      NoteListView(presenterFactory)
      EditorView()
    }
      .navigationViewStyle(DoubleColumnNavigationViewStyle())
      .padding(.top, 8) // Space to let the user drag the window because the title is hidden.
      .frame(maxWidth: .infinity, maxHeight: .infinity)
  }
}

// TODO(saket): can this be made to work by creating a fake presenter?
//struct HomeView_Previews: PreviewProvider {
//  static var previews: some View {
//    HomeView()
//  }
//}
