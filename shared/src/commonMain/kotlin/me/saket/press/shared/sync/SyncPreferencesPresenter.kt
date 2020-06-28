package me.saket.press.shared.sync

import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableFromFunction
import com.badoo.reaktive.observable.observableOfNever
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.wrap
import me.saket.press.shared.DeepLinks
import me.saket.press.shared.sync.SyncPreferencesEvent.AuthorizeClicked
import me.saket.press.shared.sync.SyncPreferencesUiEffect.OpenAuthorizationUrl
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.ui.Presenter

class SyncPreferencesPresenter(
  private val gitHost: GitHost,
  private val deepLinks: DeepLinks
) : Presenter<SyncPreferencesEvent, SyncPreferencesUiModel, SyncPreferencesUiEffect>() {

  override fun defaultUiModel(): SyncPreferencesUiModel {
    return SyncPreferencesUiModel()
  }

  override fun uiModels(): ObservableWrapper<SyncPreferencesUiModel> {
    return observableFromFunction { defaultUiModel() }.wrap()
  }

  override fun uiEffects(): ObservableWrapper<SyncPreferencesUiEffect> {
    return viewEvents().publish { events ->
      merge(
          requestAuthorization(events),
          completeAuthorization()
      )
    }.wrap()
  }

  private fun requestAuthorization(events: Observable<SyncPreferencesEvent>) =
    events.ofType<AuthorizeClicked>()
        .map { OpenAuthorizationUrl(gitHost.authorizationRequestUrl()) }

  private fun completeAuthorization() =
    deepLinks.listen()
        .filter { it.url.startsWith("intent://press/authorization-granted") }
        .flatMapCompletable { gitHost.completeAuthorization(it.url) }
        .andThen(observableOfNever<SyncPreferencesUiEffect>())
}
