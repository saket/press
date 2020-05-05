//
// Created by Saket Narayan on 5/4/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import Combine
import shared
import SwiftUI

struct EditorView: View {
  @State var editorText: String = ""

  @EnvironmentObject var theme: AppTheme

  var body: some View {
    ZStack {
      Color(theme.palette.window.editorBackgroundColor)
      MultiLineTextField(text: self.$editorText)
        .style(EditorUiStyles().editor)
        .padding(.all, 20)
    }
      .frame(maxWidth: 750)
  }
}

// Swap out the impl. when an official multi-line TextView is released by SwiftUI.
func MultiLineTextField(text: Binding<String>) -> some View {
  return MacEditorTextField(text: text) { view in
    view.isRichText = false
  }
}
