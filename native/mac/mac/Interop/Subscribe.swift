//
// Created by Saket Narayan on 5/9/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Combine
import Foundation
import SwiftUI
import shared

/// A convenience pass-through View to hide away the verbosity of subscribing
/// to a presenter's streams. Usage:
///
/// struct FooView: View {
///   @Subscribable var presenter: FooPresenter
///
///   var body: some View {
///     Subscribe($presenter) { model ->
///       Text(model.name)
///     }
///   }
///
///   init() {
///     _presenter = .init(FooPresenter())
///   }
/// }
struct Subscribe<Content, EV, M>: View
  where Content: View, EV: AnyObject, M: AnyObject {

  typealias ContentBuilder = (_ model: M) -> Content
  private let content: ContentBuilder

  private var streams: PresenterStreams<EV, M>
  @State var currentModel: M

  public init(
    _ streams: PresenterStreams<EV, M>,
    @ViewBuilder content: @escaping ContentBuilder
  ) {
    self.content = content
    self.streams = streams
    self._currentModel = State(initialValue: streams.presenter.defaultUiModel())
  }

  var body: some View {
    content(currentModel)
      // onReceive() will manage the lifecycle of this stream.
      .onReceive(streams.models) { model in
        self.currentModel = model
      }
  }
}

/// Provides transparent access to a presenter in Views while also offering
/// an easy way to use the presenter with `Subscribe`. Without this, Views
/// will have to hold onto both the presenter and PresenterStreams. The Type
/// parameters look ugly, but usages should never see them.
@propertyWrapper struct Subscribable<EV, M, P: Presenter<EV, M>>
  where EV: AnyObject, M: AnyObject {

  var wrappedValue: P
  public var projectedValue: PresenterStreams<EV, M>

  public init(_ wrappedValue: P) {
    self.wrappedValue = wrappedValue
    self.projectedValue = PresenterStreams(wrappedValue)
  }
}

/// Container for presenter streams which can't be kept inside `Subscribe`,
/// because Views get recreated on every state update causing the streams
/// to get disposed and re-subscribed.
public class PresenterStreams<EV: AnyObject, M: AnyObject> {
  public let presenter: Presenter<EV, M>
  public let models: AnyPublisher<M, Never>

  init(_ presenter: Presenter<EV, M>) {
    self.presenter = presenter

    self.models = ReaktiveInterop.asPublisher(presenter.uiModels())
      .receive(on: RunLoop.main)
      .assertNoFailure()
      .shareReplay(bufferSize: 1)
      .eraseToAnyPublisher()
  }
}
