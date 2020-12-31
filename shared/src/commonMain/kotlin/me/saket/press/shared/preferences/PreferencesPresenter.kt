package me.saket.press.shared.preferences

import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.observable.wrap
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.preferences.PreferenceCategory.About
import me.saket.press.shared.preferences.PreferenceCategory.Editor
import me.saket.press.shared.preferences.PreferenceCategory.LookAndFeel
import me.saket.press.shared.preferences.PreferenceCategory.Sync
import me.saket.press.shared.sync.SyncPreferencesScreenKey
import me.saket.press.shared.ui.Presenter

class PreferencesPresenter(
  private val strings: Strings
) : Presenter<Nothing, PreferencesModel>() {

  override fun models(): ObservableWrapper<PreferencesModel> {
    return observableOf(
      PreferencesModel(
        categories = listOf(
          PreferenceCategoryItemModel(
            title = strings.prefs.category_title_look_and_feel,
            subtitle = strings.prefs.category_subtitle_look_and_feel,
            category = LookAndFeel,
            screenKey = SyncPreferencesScreenKey  // todo.
          ),
          PreferenceCategoryItemModel(
            title = strings.prefs.category_title_editor,
            subtitle = strings.prefs.category_subtitle_editor,
            category = Editor,
            screenKey = SyncPreferencesScreenKey
          ),
          PreferenceCategoryItemModel(
            title = strings.prefs.category_title_sync,
            subtitle = strings.prefs.category_subtitle_sync,
            category = Sync,
            screenKey = SyncPreferencesScreenKey
          ),
          PreferenceCategoryItemModel(
            title = strings.prefs.category_title_about_press,
            subtitle = strings.prefs.category_subtitle_about_press,
            category = About,
            screenKey = SyncPreferencesScreenKey
          )
        )
      )
    ).wrap()
  }
}
