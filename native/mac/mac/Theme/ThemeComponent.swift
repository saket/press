//
// Created by Saket Narayan on 5/1/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Swinject
import shared
import Combine

class ThemeComponent: Assembly {
  func assemble(container: Container) {
    container.register(AppTheme.self) { _ in
      AppTheme(palette: DraculaThemePalette())
    }
  }
}
