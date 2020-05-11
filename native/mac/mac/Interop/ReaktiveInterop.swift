//
// Created by Saket Narayan on 4/27/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Combine
import shared

/// Adapts Reaktive streams as Combine streams so that they can be used with SwiftUI.
/// Copied from: https://github.com/CombineCommunity/RxCombine/.
class ReaktiveInterop {

  static func asPublisher<T: AnyObject>(
    _ reaktive: ReaktiveObservableWrapper<T>
  ) -> AnyPublisher<T, ReaktiveError> {
    return ReaktivePublisher(upstream: reaktive).eraseToAnyPublisher()
  }
}

private class ReaktivePublisher<T: AnyObject>: Publisher {
  typealias Output = T
  typealias Failure = ReaktiveError

  private let upstream: ReaktiveObservableWrapper<Output>

  init(upstream: ReaktiveObservableWrapper<Output>) {
    self.upstream = upstream
  }

  func receive<S: Subscriber>(subscriber: S) where S.Input == Output, S.Failure == Failure {
    let subscription = ReaktiveSubscription(upstream: upstream, downstream: subscriber)
    subscriber.receive(subscription: subscription)
  }
}

public struct ReaktiveError: Error {
  let throwable: KotlinThrowable
}
