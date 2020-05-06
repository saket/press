//
// Created by Saket Narayan on 5/6/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import AppKit

extension NSTextView {
  func setPaddings(horizontal: CGFloat, vertical: CGFloat) {
    /// Applying padding to the line fragment lets the user click anywhere
    /// on the padding and focus the first/last letter in the line.
    textContainer!.lineFragmentPadding = horizontal
    textContainerInset = NSSize(width: 0, height: vertical)
  }
}
