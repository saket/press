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
    ZStack {
      Color(theme.palette.window.editorBackgroundColor)

      MultiLineTextField(text: $editorText) { view in
        view.isRichText = false
        view.applyStyle(EditorUiStyles().editor)
        view.setPaddings(horizontal: 25, vertical: 35)
      }
    }
      .frame(maxWidth: 750)
  }
}
