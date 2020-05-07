//
// Created by Saket Narayan on 5/4/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Combine
import shared
import SwiftUI

struct EditorView: View {
  @EnvironmentObject var theme: AppTheme
  @State var editorText: String = ""

  var body: some View {
    ZStack(alignment: .topLeading) {
      Color(theme.palette.window.editorBackgroundColor)

      MultiLineTextField(text: $editorText) { view in
        view.textColor = NSColor(self.theme.palette.textColorPrimary)
        view.isRichText = false
        view.applyStyle(EditorUiStyles().editor)
        view.setPaddings(horizontal: 25, vertical: 35)
      }

      Text("Placeholder text")
        .style(EditorUiStyles().editor)
        .offset(x: 25, y: 35)
        .foregroundColor(theme.palette.textColorHint)

    }.frame(maxWidth: 750)
  }
}
