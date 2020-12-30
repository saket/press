package me.saket.press.shared.sync.git

interface DeviceInfo {
  /**
   * Dedicated location for storing files that other apps can't access.
   * Press currently uses this for syncing notes to a git repository.
   */
  val appStorage: File

  /**
   * Currently used by [GitSyncer] for identifying
   * this device when setting up syncing.
   */
  fun deviceName(): String

  fun supportsSplitScreen(): Boolean
}
