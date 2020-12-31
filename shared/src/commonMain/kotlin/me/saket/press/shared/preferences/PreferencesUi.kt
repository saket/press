package me.saket.press.shared.preferences

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
object PreferencesScreenKey : ScreenKey

abstract class PreferenceCategoryScreenKey(val category: PreferenceCategory) : ScreenKey

data class PreferencesModel(
  val categories: List<PreferenceCategoryItemModel>
)

data class PreferenceCategoryItemModel(
  val title: String,
  val subtitle: String,
  val category: PreferenceCategory,
  val screenKey: PreferenceCategoryScreenKey
)

enum class PreferenceCategory {
  LookAndFeel,
  Editor,
  Sync,
  About
}
