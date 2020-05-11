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
///     Subscribe($presenter) { model, effects ->
///       Text(model.name)
///     }
///   }
///
///   init() {
///     _presenter = .init(FooPresenter())
///   }
/// }
struct Subscribe<Content, EV, M, EF>: View
  where Content: View, EV: AnyObject, M: AnyObject, EF: AnyObject {

  typealias ContentBuilder = (_ model: M, _ effects: AnyPublisher<EF, Never>) -> Content
  private let content: ContentBuilder

  private var streams: PresenterStreams<EV, M, EF>
  @State var currentModel: M

  public init(
    _ streams: PresenterStreams<EV, M, EF>,
    @ViewBuilder content: @escaping ContentBuilder
  ) {
    self.content = content
    self.streams = streams
    self._currentModel = State(initialValue: streams.presenter.defaultUiModel()!)
  }

  var body: some View {
    content(currentModel, streams.uiEffects)
      // onReceive() will manage the lifecycle of this stream.
      .onReceive(streams.uiModels) { model in
        self.currentModel = model
      }
  }
}

/// Provides transparent access to a presenter in Views while also offering
/// an easy way to use the presenter with `Subscribe`. Without this, Views
/// will have to hold onto both the presenter and PresenterStreams. The Type
/// parameters look ugly, but usages should never see them.
@propertyWrapper struct Subscribable<EV, M, EF, P: Presenter<EV, M, EF>>
  where EV: AnyObject, M: AnyObject, EF: AnyObject {

  var wrappedValue: P
  public var projectedValue: PresenterStreams<EV, M, EF>

  public init(_ wrappedValue: P) {
    self.wrappedValue = wrappedValue
    self.projectedValue = PresenterStreams(wrappedValue)
  }
}

/// Container for presenter streams which can't be kept inside `Subscribe`,
/// because Views get recreated on every state update causing the streams
/// to get disposed and re-subscribed.
public struct PresenterStreams<Event, Model, Effect>
  where Event: AnyObject, Model: AnyObject, Effect: AnyObject {

  public let presenter: Presenter<Event, Model, Effect>
  public let uiModels: AnyPublisher<Model, Never>
  public let uiEffects: AnyPublisher<Effect, Never>

  init(_ presenter: Presenter<Event, Model, Effect>) {
    self.presenter = presenter

    self.uiModels = ReaktiveInterop.asPublisher(presenter.uiModels())
      .receive(on: RunLoop.main)
      .assertNoFailure()
      .shareReplay(bufferSize: 1)
      .makeConnectable()
      .autoconnect()
      .eraseToAnyPublisher()

    self.uiEffects = ReaktiveInterop.asPublisher(presenter.uiEffects())
      .receive(on: RunLoop.main)
      .assertNoFailure()
      .shareReplay(bufferSize: 1)
      .makeConnectable()
      .autoconnect()
      .eraseToAnyPublisher()
  }
}
