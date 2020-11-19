package me.saket.press.shared.sync.stats

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
object SyncStatsForNerdsScreenKey : ScreenKey

data class SyncStatsForNerdsUiModel(
  val gitDirectorySize: String,
  val logs: String
)
