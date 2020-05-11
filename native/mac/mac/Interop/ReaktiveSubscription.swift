//
// Created by Saket Narayan on 5/3/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Combine
import shared

/// Copied from: https://github.com/CombineCommunity/RxCombine/.
class ReaktiveSubscription<U: AnyObject, D: Subscriber>: Subscription
  where D.Failure == ReaktiveError, D.Input == U {

  private var disposable: ReaktiveDisposable?
  private let buffer: DemandBuffer<D>

  init(upstream: ReaktiveObservableWrapper<U>, downstream: D) {
    buffer = DemandBuffer(subscriber: downstream)
    disposable = upstream.subscribe(
      isThreadLocal: false,
      onSubscribe: nil,
      onError: { e in
        NSLog("Error received: \(e.message)")
        e.printStackTrace()
        self.buffer.complete(completion: .failure(ReaktiveError(throwable: e)))
      },
      onComplete: {
        self.buffer.complete(completion: .finished)
      },
      onNext: { (u: U?) in
        _ = self.buffer.buffer(value: u!)
      }
    )
  }

  func request(_ demand: Subscribers.Demand) {
    _ = self.buffer.demand(demand)
  }

  func cancel() {
    disposable?.dispose()
    disposable = nil
  }
}

/// A buffer responsible for managing the demand of a downstream
/// subscriber for an upstream publisher.
///
/// It buffers values and completion events and forwards them
/// according to the demand requested by the downstream.
///
/// In a sense, the subscription only relays the requests for
/// demand, as well the events emitted by the upstream — to this
/// buffer, which manages the entire behavior and back-pressure
/// contract.
private class DemandBuffer<S: Subscriber> {
  private let lock = NSRecursiveLock()
  private var buffer = [S.Input]()
  private let subscriber: S
  private var completion: Subscribers.Completion<S.Failure>?
  private var demandState = Demand()

  init(subscriber: S) {
    self.subscriber = subscriber
  }

  /// Buffer an upstream value to later be forwarded to the downstream
  /// subscriber, once it demands it.
  ///
  /// - parameter value: Upstream value to buffer
  ///
  /// - returns: The demand fulfilled by the buffer.
  func buffer(value: S.Input) -> Subscribers.Demand {
    precondition(self.completion == nil, "Received values after termination")

    switch demandState.requested {
    case .unlimited:
      return subscriber.receive(value)
    default:
      buffer.append(value)
      return flush()
    }
  }

  /// Complete the demand buffer with an upstream completion event. This will
  /// deplete the buffer immediately, based on the currently accumulated demand,
  /// and relay the completion event down as soon as demand is fulfilled.
  ///
  /// - parameter completion: Completion event
  func complete(completion: Subscribers.Completion<S.Failure>) {
    precondition(self.completion == nil, "Completion has already occurred")
    self.completion = completion
    _ = flush()
  }

  /// Signal to the buffer that the downstream requested new demand
  ///
  /// - note: The buffer will attempt to flush as many events requested
  ///         by the downstream at this point
  func demand(_ demand: Subscribers.Demand) -> Subscribers.Demand {
    flush(adding: demand)
  }

  /// Flush buffered events to the downstream based on the current state of the
  /// downstream's demand
  ///
  /// - parameter newDemand: The new demand to add. If `nil`, the flush isn't the
  /// result of an explicit demand change
  ///
  /// - note: After fulfilling the downstream's request, if completion has already
  /// occurred, the buffer will be cleared and the completion event will be sent
  /// to the downstream subscriber.
  private func flush(adding newDemand: Subscribers.Demand? = nil) -> Subscribers.Demand {
    lock.lock()
    defer { lock.unlock() }

    if let newDemand = newDemand {
      demandState.requested += newDemand
    }

    // If buffer isn't ready for flushing, return immediately
    guard demandState.requested > 0 || newDemand == Subscribers.Demand.none else {
      return .none
    }

    while !buffer.isEmpty && demandState.processed < demandState.requested {
      demandState.requested += subscriber.receive(buffer.remove(at: 0))
      demandState.processed += 1
    }

    if let completion = completion {
      // Completion event was already sent
      self.buffer = []
      self.demandState = .init()
      self.completion = nil
      self.subscriber.receive(completion: completion)
      return .none
    }

    let sentDemand = demandState.requested - demandState.sent
    demandState.sent += sentDemand
    return sentDemand
  }
}

/// Tracks the downstream's accumulated demand state
private extension DemandBuffer {
  struct Demand {
    var processed: Subscribers.Demand = .none
    var requested: Subscribers.Demand = .none
    var sent: Subscribers.Demand = .none
  }
}

