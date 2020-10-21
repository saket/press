package me.saket.press.shared.sync.stats

import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.startWithValue
import com.badoo.reaktive.observable.wrap
import com.soywiz.klock.seconds
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.observableInterval
import me.saket.press.shared.sync.Syncer
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.util.format

class SyncStatsForNerdsPresenter(
  private val syncer: Syncer,
  private val strings: Strings,
  private val schedulers: Schedulers
) : Presenter<Nothing, SyncStatsForNerdsUiModel, Nothing>() {

  override fun defaultUiModel(): SyncStatsForNerdsUiModel {
    return SyncStatsForNerdsUiModel(
        gitDirectorySize = strings.sync.nerd_stats_git_size.format("...")
    )
  }

  override fun uiModels(): ObservableWrapper<SyncStatsForNerdsUiModel> {
    return observableInterval(startDelay = 0, period = 0.5.seconds, schedulers.computation)
        .map {
          SyncStatsForNerdsUiModel(
              gitDirectorySize = syncer.directorySize().presentableText()
          )
        }
        .startWithValue(defaultUiModel())
        .wrap()
  }

  private fun FileSize?.presentableText(): String {
    val text = if (this != null) {
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
}
