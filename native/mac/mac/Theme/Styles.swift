//
// Created by Saket Narayan on 5/2/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import SwiftUI
import shared

extension View {
  func style(_ style: UiStyles.Text) -> some View {
    return modifier(TextStyleModifier(style: style))
  }
}

extension NSTextView {
  func applyStyle(_ style: UiStyles.Text) {
    if (style.maxLines != nil) {
      fatalError("Unsupported")
    }

    self.defaultParagraphStyle = NSMutableParagraphStyle().apply {
      $0.lineSpacing = CGFloat(style.lineSpacing)
    }
    let fontName = style.resolveFontName()
    self.font = NSFont(name: fontName, size: CGFloat(style.textSize))
  }
}

// PSA: AppCode fails to identify ViewModifier's content
// type: https://youtrack.jetbrains.com/issue/OC-20058.
struct TextStyleModifier: ViewModifier {
  let style: UiStyles.Text

  func body(content: Content) -> some View {
    let lineLimit = style.maxLines?.intValue ?? Int.max
    let fontName = style.resolveFontName()

    return content
      .font(.custom(fontName, size: CGFloat(style.textSize)))
      .lineSpacing(CGFloat(style.lineSpacing))
      .lineLimit(lineLimit)
      .truncationMode(.tail)
  }
}

extension UiStyles.Text {
  var lineSpacing: Int {
    let lineHeight = lineSpacingMultiplier * textSize
    return Int(lineHeight - textSize)
  }

  func resolveFontName() -> String {
    switch (font.family) {
    case .workSans:
      switch font.variant {
      case .regular: return "WorkSans-Regular"
      case .bold: return "WorkSans-Bold"
      case .italic: return "WorkSans-Italic"
      default: fatalError("Unexpected font variant: \(font.variant) for \(font.family)")
      }
    default: fatalError("Unexpected font family: \(font.family)")
    }
  }
}
