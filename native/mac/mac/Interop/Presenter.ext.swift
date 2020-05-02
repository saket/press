//
// Created by Saket Narayan on 4/29/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Combine
import SwiftUI
import shared

extension View {

  public func onReceive<T>(
    _ reaktive: ReaktiveObservableWrapper<T>,
    perform: @escaping (T) -> Void
  ) -> some View where T: Equatable {
    let publisher = ReaktiveInterop.asPublisher(reaktive)
      .removeDuplicates()
      .receive(on: DispatchQueue.main)
//      .breakpointOnError()
//      .print()
      .assertNoFailure()
    return onReceive(publisher, perform: perform)
  }
}
