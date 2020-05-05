//
// Created by Saket Narayan on 4/27/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Combine
import shared

/// Adapts Reaktive streams as Combine streams so that they can be used with SwiftUI.
/// Back-pressure isn't supported, but that isn't needed in the UI layer anyway.
/// See Subscribe.swift for usage.
///
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

private class ReaktiveSubscription<U: AnyObject, D: Subscriber>: Subscription
  where D.Failure == ReaktiveError, D.Input == U {

  private var disposable: ReaktiveDisposable? = nil

  init(upstream: ReaktiveObservableWrapper<U>, downstream: D) {
    disposable = upstream.subscribe(
      isThreadLocal: false,
      onSubscribe: nil,
      onError: { e in
        downstream.receive(completion: .failure(ReaktiveError(throwable: e)))
      },
      onComplete: {
        downstream.receive(completion: .finished)
      },
      onNext: { (u: U?) in
        let remaining = downstream.receive(u!)
        precondition(remaining == Subscribers.Demand.max(0), "A non-zero demand isn't supported")
      }
    )
  }

  func request(_ demand: Subscribers.Demand) {
    precondition(demand == Subscribers.Demand.unlimited, "A finite demand isn't supported")
  }

  func cancel() {
    disposable?.dispose()
  }
}
