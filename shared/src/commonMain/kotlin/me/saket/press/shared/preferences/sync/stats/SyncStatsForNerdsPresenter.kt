package me.saket.press.shared.preferences.sync.stats

import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.combineLatest
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.flatMap
import com.badoo.reaktive.observable.startWithValue
import com.badoo.reaktive.observable.wrap
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.asObservable
import com.badoo.reaktive.single.observeOn
import com.badoo.reaktive.single.singleFromFunction
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.syncer.Syncer
import me.saket.press.shared.syncer.git.File
import me.saket.press.shared.syncer.git.FileBasedSyncLogger
import me.saket.press.shared.syncer.git.children
import me.saket.press.shared.syncer.git.existsOrNull
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.util.format

class SyncStatsForNerdsPresenter(
  private val syncer: Syncer,
  private val strings: Strings,
  private val schedulers: Schedulers,
) : Presenter<Nothing, SyncStatsForNerdsUiModel>() {

  override fun defaultUiModel(): SyncStatsForNerdsUiModel {
    return SyncStatsForNerdsUiModel(
      gitDirectorySize = strings.sync.nerd_stats_git_size.format("..."),
      logs = ""
    )
  }

  override fun models(): ObservableWrapper<SyncStatsForNerdsUiModel> {
    return syncer.status()
      .flatMap {
        val formattedSize = syncer.directory.formatSize()
          .observeOn(schedulers.computation)
          .asObservable()
          .startWithValue(strings.sync.nerd_stats_git_size.format("..."))
        val logs = readSyncLogs()
          .observeOn(schedulers.io)
          .asObservable()
          .startWithValue("")
        combineLatest(formattedSize, logs, ::SyncStatsForNerdsUiModel)
      }
      .distinctUntilChanged()
      .wrap()
  }

  private fun File.formatSize(): Single<String> {
    return singleFromFunction {
      val bytes = try {
        children(recursively = true).map { it.sizeInBytes() }.sum()
      } catch (e: Throwable) {
        e.printStackTrace()
        null
      }

      strings.sync.nerd_stats_git_size.format(
        if (bytes != null) {
          val kb = bytes / 1024
          val mb = kb / 1024
          val gb = mb / 1024

          when {
            mb < 1 -> "~$kb KB"
            gb < 1 -> "~$mb MB"
            else -> "~$gb GB"
          }
        } else {
          strings.sync.nerd_stats_git_size_unavailable
        }
      )
    }
  }

  private fun readSyncLogs(): Single<String> {
    return singleFromFunction {
      FileBasedSyncLogger(syncer.directory).file
        .existsOrNull()
        ?.read()
        ?: strings.sync.nerd_stats_emptystate
    }
  }
}
