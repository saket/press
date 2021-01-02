package me.saket.press.shared.preferences

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.preferences.PreferenceCategory.AboutApp
import me.saket.press.shared.preferences.PreferenceCategory.Editor
import me.saket.press.shared.preferences.PreferenceCategory.LookAndFeel
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
object PreferencesScreenKey : ScreenKey

@AndroidParcelize
data class PreferenceCategoryScreenKey(
  val category: PreferenceCategory
) : ScreenKey

data class PreferencesModel(
  val categories: List<PreferenceCategoryItemModel>
)

data class PreferenceCategoryItemModel(
  val title: String,
  val subtitle: CharSequence,
  val category: PreferenceCategory
)

enum class PreferenceCategory {
  LookAndFeel,
  Editor,
  Sync,
  AboutApp
}
