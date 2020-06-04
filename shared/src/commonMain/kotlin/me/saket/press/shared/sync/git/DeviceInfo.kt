package me.saket.press.shared.sync.git

import com.benasher44.uuid.Uuid
import me.saket.press.shared.settings.Setting

data class DeviceInfo(
  /**
   * Dedicated location for storing files that other apps can't access.
   * Press currently uses this for syncing notes to a git repository.
   */
  val appStorage: File,

  /**
   * Currently used by [GitSyncer] for identifying
   * this device when setting up syncing.
   */
  val deviceName: () -> String
)

/** Setting for storing a unique ID for Press on this device. */
inline class DeviceId(val id: Uuid)
