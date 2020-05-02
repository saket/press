//
// Created by Saket Narayan on 4/26/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Swinject
import shared

class HomeComponent: Assembly {

  func assemble(container: Container) {
    container.register(HomeView.self) { r in
      HomeView(presenterFactory: r.resolve(HomePresenterFactory.self)!)
    }

    container.register(HomePresenterFactory.self) { r in
      class Factory: HomePresenterFactory {
        func create(args: HomePresenter.Args) -> HomePresenter {
          SharedHomeComponent.Companion().presenter(args: args)
        }
      }
      return Factory()
    }
  }
}
