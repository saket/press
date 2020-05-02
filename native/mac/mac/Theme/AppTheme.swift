//
// Created by Saket Narayan on 5/1/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Combine
import shared

public class AppTheme : ObservableObject {
  @Published var palette: ThemePalette

  init(palette: ThemePalette) {
    self.palette = palette
  }
}
