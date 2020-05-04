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

  var body: some View {
    return ZStack {
      Color(theme.palette.window.editorBackgroundColor)
    }
  }
}
