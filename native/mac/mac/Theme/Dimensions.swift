//
// Created by Saket Narayan on 5/4/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation

struct Dimensions {
  static let window: CGSize = .init(width: 980, height: 580)
  static let windowTitleBarHeight: CGFloat = 21

  static let noteListWidth: CGFloat = window.width * 0.3
  static let editorWidth: CGFloat = window.width - noteListWidth
}
