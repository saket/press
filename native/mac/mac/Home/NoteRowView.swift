//
// Created by Saket Narayan on 4/30/20.
// Copyright (c) 2020 Saket Narayan. All rights reserved.
//

import Foundation
import SwiftUI
import shared

struct NoteRowView: View {
  let note: HomeUiModel.Note
  @EnvironmentObject var theme: AppTheme

  var body: some View {
    VStack(alignment: .leading, spacing: 8) {
      Text(note.title)
        .lineLimit(1)
        .truncationMode(.tail)
        .frame(maxWidth: .infinity, alignment: .leading)

      Text(note.body)
        .lineLimit(2)
        .truncationMode(.tail)
        .frame(maxWidth: .infinity, alignment: .leading)
    }.padding(8)
  }
}
