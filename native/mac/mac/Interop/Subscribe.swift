//
// Created by Saket Narayan on 5/3/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Combine
import Foundation
import SwiftUI
import shared

/// A convenience pass-through View to hide away the
/// verbosity of subscribing to a presenter's streams.
struct Subscribe<Content, Event, Model, Effect>: View
  where Content: View, Event: AnyObject, Model: AnyObject, Effect: AnyObject {

  private let content: ContentBuilder
  typealias ContentBuilder = (_ model: Model, _ effects: AnyPublisher<Effect, Never>) -> Content

  // It's important to keep these streams here as properties instead
  // of creating them in the body to avoid them from getting deallocated.
  // If they get deallocated, SwiftUI refreshes this View, causing an
  // infinite loop where the streams get subscribed-to and canceled forever.
  private let uiModels: AnyPublisher<Model, Never>
  private let uiEffects: AnyPublisher<Effect, Never>
  @State var currentModel: Model

  public init(
    _ presenter: Presenter<Event, Model, Effect>,
    @ViewBuilder content: @escaping ContentBuilder
  ) {
    self.content = content
    self._currentModel = State(initialValue: presenter.defaultUiModel()!)

    self.uiModels = ReaktiveInterop.asPublisher(presenter.uiModels())
      .receive(on: RunLoop.main)
      .assertNoFailure()
      .eraseToAnyPublisher()

    self.uiEffects = ReaktiveInterop.asPublisher(presenter.uiEffects())
      .receive(on: RunLoop.main)
      .assertNoFailure()
      .eraseToAnyPublisher()
  }

  var body: some View {
    content(currentModel, uiEffects)
      // SwiftUI will manage the lifecycle of this models stream.
      .onReceive(uiModels) { model in
        self.currentModel = model
      }
  }
}
