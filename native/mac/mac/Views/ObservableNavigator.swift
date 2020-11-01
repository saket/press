//
// Created by Saket Narayan on 11/1/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Combine
import shared

class ObservableNavigator: Navigator {
  @Published var screenKey: ScreenKey? = nil

  func lfg(screen: ScreenKey) {
    screenKey = screen
  }

  func listen<T>(_ type: T.Type) -> AnyPublisher<T, Never> {
    return $screenKey.ofType(type)
  }
}
