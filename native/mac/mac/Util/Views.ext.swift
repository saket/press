//
// Created by Saket Narayan on 5/6/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import AppKit
import SwiftUI

extension NSTextView {
  func setPaddings(horizontal: CGFloat, vertical: CGFloat) {
    /// Applying padding to the line fragment lets the user click anywhere
    /// on the padding and focus the first/last letter in the line.
    textContainer!.lineFragmentPadding = horizontal
    textContainerInset = NSSize(width: 0, height: vertical)
  }
}

extension List {
  /// List on macOS uses an opaque background with no option for
  /// removing/changing it. listRowBackground() doesn't work either.
  /// This workaround works because List is backed by NSTableView
  /// on macOS.
  func removeBackground() -> some View {
    return introspectTableView { tableView in
      tableView.backgroundColor = .clear
      tableView.enclosingScrollView!.drawsBackground = false

      /// SwiftUI doesn't offer any way to set highlight
      /// colors so we draw them manually instead.
      tableView.selectionHighlightStyle = .none
    }
  }
}

extension View {
  /// SwiftUI has additional spacing around list items on macOS
  /// with option for removing them. Using negative margins until
  /// this is fixed.
  func removeListMargins() -> some View {
    return padding(.horizontal, -8).padding(.vertical, -4)
  }
}
