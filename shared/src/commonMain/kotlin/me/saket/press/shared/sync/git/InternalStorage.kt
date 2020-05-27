package me.saket.press.shared.sync.git

/**
 * Dedicated location for storing files that other apps can't access.
 * Press currently uses this for syncing notes to a git repository.
 *
 * todo: could be replaced with [File].
 */
data class InternalStorage(val path: String)
