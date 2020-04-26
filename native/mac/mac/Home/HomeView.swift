//
//  ContentView.swift
//  mac
//
//  Created by Saket Narayan on 4/15/20.
//  Copyright © 2020 Saket Narayan. All rights reserved.
//

import SwiftUI
import shared

// SwiftUI doesn't allow mutation of @State objects directly.
// Wrapping it inside an observable object does the trick.
class ObservablePlatform : ObservableObject {
  @Published var name: String = "default value"
}

struct HomeView: View {
  let presenterFactory: HomePresenterFactory
  @State private var platform = ObservablePlatform()

  var body: some View {
    return Text("Hello \(platform.name)!")
      .frame(maxWidth: .infinity, maxHeight: .infinity)
  }

  init(presenterFactory: HomePresenterFactory) {
    self.presenterFactory = presenterFactory

    // Inlining this function here fails with an error because
    // updateName() is capturing "self" in a closure, but which
    // isn't allowed in structs, not sure why. Gotta figure out
    // a better way.
    updateName()
  }

  func updateName() {
    TestPresenter()
      .platformNameWrapper()
      .subscribe(isThreadLocal: false, onSubscribe: nil, onError: nil, onSuccess: { name in
        self.platform.name = "\(name! as String)"
      })
  }
}

// TODO(saket): can this be made to work by creating fake presenters?
//struct HomeView_Previews: PreviewProvider {
//  static var previews: some View {
//    HomeView()
//  }
//}
