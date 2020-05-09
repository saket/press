//
// Created by Saket Narayan on 5/7/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Combine
import shared

extension Publisher {

  /// FIXME: Using Self.Failure doesn't seem to work on publishers
  /// that already have a Failure type of Never. Need help.
  public func ofType<F>(_ class: F.Type) -> AnyPublisher<F, Never> {
    filter { $0 is F }
      .map { $0 as! F }
      .assertNoFailure()
      .eraseToAnyPublisher()
  }
}
