package me.saket.press.shared.sync

import me.saket.press.data.shared.Note

/** Syncs notes to a remote destination. */
interface Syncer {

  /**
   * Called every time a note's content is updated,
   * including when it's created for the first time.
   */
  fun onUpdateContent(note: Note)
}
