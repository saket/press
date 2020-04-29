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
//class ObservableHomeUiModel: ObservableObject {
//  @Published var model: HomeUiModel = HomeUiModel(notes: [])
//}

struct HomeView: View {
  // todo: offer default viewmodel from somewhere else?
  //@State private var model = ObservableHomeUiModel()
  private var cancelable: AnyCancellable? = nil
  private var cancelable2: AnyCancellable? = nil
  private var disposable: ReaktiveDisposable? = nil

  var body: some View {
    return Text("Hello macOS!")
      .frame(maxWidth: .infinity, maxHeight: .infinity)
      .onDisappear {
        self.cancelable?.cancel()
        self.cancelable2?.cancel()
        self.disposable?.dispose()
      }
  }

  init(presenterFactory: HomePresenterFactory) {
    // Inlining this function here fails with an error because
    // updateName() is capturing "self" in a closure, but which
    // isn't allowed in structs, not sure why. Gotta figure out
    // a better way.
    //disposable = subscribeToUiModels(presenterFactory)

    let stream = TestPresenter().streamWithAsyncValues()

    print("HomeView: Subscribing to stream")
    cancelable = ReaktiveInterop.asPublisher(reaktive: stream)
      //.subscribe(on: RunLoop.main)
      .receive(on: DispatchQueue.main)
      .sink(receiveCompletion: { completion in
        print("HomeView: Publisher completed on thread \(Thread.current.threadName)")
      }, receiveValue: { string in
        print("HomeView: Publisher emitted: \(string) on thread \(Thread.current.threadName)")
      })
  }
}

// TODO(saket): can this be made to work by creating a fake presenter?
//struct HomeView_Previews: PreviewProvider {
//  static var previews: some View {
//    HomeView()
//  }
//}

extension Thread {
  var threadName: String {
    if let currentOperationQueue = OperationQueue.current?.name {
      return "OperationQueue: \(currentOperationQueue)"
    } else if let underlyingDispatchQueue = OperationQueue.current?.underlyingQueue?.label {
      return "DispatchQueue: \(underlyingDispatchQueue)"
    } else {
      let name = __dispatch_queue_get_label(nil)
      return String(cString: name, encoding: .utf8) ?? Thread.current.description
    }
  }
}
