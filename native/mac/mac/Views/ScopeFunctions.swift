//
// Created by Saket Narayan on 5/6/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation

/// La-kotlin style scope functions.
protocol ScopeFunc {}
extension ScopeFunc {
  @inline(__always) func apply(_ block: (_ it: Self) -> ()) -> Self {
    block(self)
    return self
  }
  @inline(__always) func with<R>(_ block: (Self) -> R) -> R {
    return block(self)
  }
}
extension NSObject: ScopeFunc {}
