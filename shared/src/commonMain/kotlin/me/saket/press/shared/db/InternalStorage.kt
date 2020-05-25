package me.saket.press.shared.db

/**
 * Dedicated location for storing files that other apps can't access.
 * Press currently uses this for syncing notes to a git repository.
 */
data class InternalStorage(val path: String)
