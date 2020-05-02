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

// PSA: AppCode fails to identify ViewModifier's content
// type: https://youtrack.jetbrains.com/issue/OC-20058.
struct TextStyleModifier: ViewModifier {
  let style: UiStyles.Text

  func body(content: Content) -> some View {
    let lineLimit = style.maxLines?.intValue ?? Int.max
    let fontName = resolveFontName(style.font)

    return content
      .font(.custom(fontName, size: CGFloat(style.textSize)))
      .lineLimit(lineLimit)
      .truncationMode(.tail)
  }

  private func resolveFontName(_ font: UiStyles.Font) -> String {
    switch (style.font.family) {
    case .workSans:
      switch style.font.variant {
      case .regular: return "WorkSans-Regular"
      case .bold: return "WorkSans-Bold"
      case .italic: return "WorkSans-Italic"
      default: fatalError("Unexpected font variant: \(style.font.variant) for \(style.font.family)")
      }
    default: fatalError("Unexpected font family: \(style.font.family)")
    }
  }
}
