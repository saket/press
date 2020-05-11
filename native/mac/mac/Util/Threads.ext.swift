//
// Created by Saket Narayan on 5/10/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation

extension Thread {

  static func printStackTrace() {
    for symbol: String in Thread.callStackSymbols {
      print(symbol)
    }
  }

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
