//
// Created by Saket Narayan on 5/2/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import SwiftUI

extension View {
  @inlinable func foregroundColor(_ argb: Int32) -> some View {
    return foregroundColor(Color(argb))
  }
}

public extension Color {
  init(_ argb: Int32) {
    let (red, green, blue, alpha) = readColorChannels(argb)
    self.init(
      red: Double(red) / 255.0,
      green: Double(green) / 255.0,
      blue: Double(blue) / 255.0,
      opacity: Double(alpha) / 255.0
    )
  }
}

public extension NSColor {
  convenience init(_ argb: Int32) {
    let (red, green, blue, alpha) = readColorChannels(argb)
    let redFloat = CGFloat(red) / 255.0
    let greenF = CGFloat(green) / 255.0
    let blueF = CGFloat(blue) / 255.0
    let alphaF = CGFloat(alpha) / 255.0

    self.init(
      red: redFloat,
      green: greenF,
      blue: blueF,
      alpha: alphaF
    )
  }
}

private func readColorChannels(_ argb: Int32) -> (Int32, Int32, Int32, Int32) {
  let red = (argb >> 16) & 0xFF
  let green = (argb >> 8) & 0xFF
  let blue = argb & 0xFF
  let alpha = (argb >> 24) & 0xFF
  return (red, green, blue, alpha)
}
