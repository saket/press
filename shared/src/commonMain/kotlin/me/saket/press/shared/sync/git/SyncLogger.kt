package me.saket.press.shared.sync.git

import co.touchlab.stately.collections.IsoMutableList
import com.soywiz.klock.DateTime

interface SyncLogger {
  fun log(message: String)
  fun onSyncStart() = Unit
  fun onSyncComplete()
}

class SyncLoggers(private vararg val defaultLoggers: SyncLogger) : SyncLogger {
  private val loggers = IsoMutableList<SyncLogger>().apply {
    addAll(defaultLoggers.toList())
  }

  fun add(logger: SyncLogger) {
    loggers.add(logger)
  }

  override fun log(message: String) = loggers.forEach { it.log(message) }
  override fun onSyncStart() = loggers.forEach { it.onSyncStart() }
  override fun onSyncComplete() = loggers.forEach { it.onSyncComplete() }
}

object PrintLnSyncLogger : SyncLogger {
  override fun log(message: String) = println(message)
  override fun onSyncStart() = println("======================================")
  override fun onSyncComplete() = println()
}

/**
 * Writes logs to a file so that they can optionally be shared by users to debug sync issues.
 * Git maintains its revision history so the logs are reset on every sync and Press does not
 * have to evict stale logs.
 */
class FileBasedSyncLogger(private val notesDirectory: File) : SyncLogger {
  private val buffer = IsoMutableList<String>()
  private val file get() = File(notesDirectory, ".press/sync_log.txt")

  override fun log(message: String) {
    buffer.add(message)
  }

  override fun onSyncStart() {
    buffer.clear()
    log("Starting sync on ${DateTime.now()}\n")
  }

  override fun onSyncComplete() {
    log("Finished sync on ${DateTime.now()}\n")
    file.write(buffer.joinToString(separator = "\n"))
    buffer.clear()
  }
}
