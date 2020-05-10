//
// Created by Saket Narayan on 4/30/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import SwiftUI
import shared

struct NoteRowView: View {
  @EnvironmentObject var theme: AppTheme
  private let style = HomeUiStyles.NoteRow()

  let note: HomeUiModel.Note

  var body: some View {
    VStack(alignment: .leading, spacing: 8) {
      Text(note.title)
        .style(style.title)
        .foregroundColor(theme.palette.textColorHeading)
        .frame(maxWidth: .infinity, alignment: .leading)

      Text(note.body)
        .style(style.body)
        .foregroundColor(theme.palette.textColorSecondary)
        .frame(maxWidth: .infinity, alignment: .leading)
    }.padding(16)
  }
}
