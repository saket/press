package me.saket.press.shared.sync.stats

import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.startWithValue
import com.badoo.reaktive.observable.wrap
import com.soywiz.klock.seconds
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.observableInterval
import me.saket.press.shared.sync.Syncer
import me.saket.press.shared.sync.git.File
import me.saket.press.shared.sync.git.FileBasedSyncLogger
import me.saket.press.shared.sync.git.children
import me.saket.press.shared.sync.git.existsOrNull
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.util.format

class SyncStatsForNerdsPresenter(
  private val syncer: Syncer,
  private val strings: Strings,
  private val schedulers: Schedulers
) : Presenter<Nothing, SyncStatsForNerdsUiModel, Nothing>() {

  override fun defaultUiModel(): SyncStatsForNerdsUiModel {
    return SyncStatsForNerdsUiModel(
      gitDirectorySize = strings.sync.nerd_stats_git_size.format("..."),
      logs = ""
    )
  }

  override fun uiModels(): ObservableWrapper<SyncStatsForNerdsUiModel> {
    return observableInterval(startDelay = 0, period = 1.seconds, schedulers.computation)
      .map {
        SyncStatsForNerdsUiModel(
          gitDirectorySize = syncer.directory.formatSize(),
          logs = readSyncLogs()
        )
      }
      .startWithValue(defaultUiModel())
      .distinctUntilChanged()
      .wrap()
  }

  private fun File.formatSize(): String {
    val bytes = try {
      children(recursively = true).map { it.sizeInBytes() }.sum()
    } catch (e: Throwable) {
      e.printStackTrace()
      null
    }

    val text = if (bytes != null) {
      val kb = bytes / 1024
      val mb = kb / 1024
      val gb = mb / 1024

      when {
        mb < 1 -> "~$kb KB"
        gb < 1 -> "~$mb MB"
        else -> "~$gb GB"
      }
    } else {
      "N / A"
    }
    return strings.sync.nerd_stats_git_size.format(text)
  }

  private fun readSyncLogs(): String {
    return FileBasedSyncLogger(syncer.directory).file
      .existsOrNull()
      ?.read()
      ?: "Logs will be shown here after the first sync"
  }
}
