//
// Created by Saket Narayan on 5/3/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Combine
import Foundation
import SwiftUI
import shared

// A convenience pass-through View to hide away the
// verbosity of subscribing to a presenter's streams.
struct Subscribe<Content, EV, M, EF>: View
  where Content: View, EV: AnyObject, M: AnyObject, EF: AnyObject {

  // todo: think of a better name.
  typealias ContentBuilder = (_ model: M, _ effects: AnyPublisher<EF, Never>) -> Content

  private let content: ContentBuilder

  @State var currentModel: M
  private let uiModels: AnyPublisher<M, Never>
  private let uiEffects: AnyPublisher<EF, Never>

  public init(
    _ presenter: Presenter<EV, M, EF>,
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
      .onReceive(uiModels) { model in
        self.currentModel = model
      }
  }
}
