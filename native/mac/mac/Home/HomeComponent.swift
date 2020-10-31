//
// Created by Saket Narayan on 4/26/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Swinject
import shared

class HomeComponent: Assembly {
  func assemble(container: Container) {
    container.register(HomePresenterFactory.self) { _ in
      class Factory: HomePresenterFactory {
        func create(args_: HomePresenter.Args) -> HomePresenter {
          SharedHomeComponent.Companion().presenter(args: args_)
        }
      }
      return Factory()
    }
  }
}
