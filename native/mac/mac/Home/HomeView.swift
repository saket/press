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

// SwiftUI doesn't allow mutation of @State objects directly.
// Wrapping it inside an observable object does the trick.
class ObservableHomeUiModel: ObservableObject {
  @Published var model: HomeUiModel = HomeUiModel(notes: [])
}

struct HomeView: View {
  // todo: offer default viewmodel from somewhere else?
  @ObservedObject private var model = ObservableHomeUiModel()
  private var cancelable: AnyCancellable? = nil

  var body: some View {
    let noteTitles = model.model.notes
      .map { note in "- \(note.title)" }
      .joined(separator: "\n")

    return Text("Notes: \n\(noteTitles)")
      .frame(maxWidth: .infinity, maxHeight: .infinity)
      .onDisappear {
        self.cancelable?.cancel()
      }
  }

  init(presenterFactory: HomePresenterFactory) {
    let args = HomePresenter.Args(includeEmptyNotes: true)
    let presenter = presenterFactory.create(args: args)

    // Inlining this function here fails with an error because
    // updateName() is capturing "self" in a closure, but which
    // isn't allowed in structs, not sure why. Gotta figure out
    // a better way.
    cancelable = streamUiModels(presenter)
  }

  private func streamUiModels(_ presenter: HomePresenter) -> AnyCancellable {
    return ReaktiveInterop
      .asPublisher(reaktive: presenter.uiModels())
      .receive(on: DispatchQueue.main)
      .sink(receiveCompletion: { _ in }, receiveValue: { model in
        print("Received models: \(model)")
        self.model.model = model
      })
  }
}

// TODO(saket): can this be made to work by creating a fake presenter?
//struct HomeView_Previews: PreviewProvider {
//  static var previews: some View {
//    HomeView()
//  }
//}
