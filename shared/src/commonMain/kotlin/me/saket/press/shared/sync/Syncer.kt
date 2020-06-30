package me.saket.press.shared.sync

/** Syncs notes with a remote destination. */
interface Syncer {

  /**
   * Called every time a note's content is updated,
   * including when it's created for the first time.
   */
  fun sync()
}
