package me.saket.press.shared.sync

import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.wrap
import io.ktor.client.HttpClient
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.mergeWith
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.SyncPreferencesEvent.DisableSyncClicked
import me.saket.press.shared.sync.SyncPreferencesEvent.SetupHostClicked
import me.saket.press.shared.sync.SyncPreferencesUiEffect.OpenUrl
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncDisabled
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncEnabled
import me.saket.press.shared.sync.Syncer.Status.Disabled
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.sync.git.GitHostAuthToken
import me.saket.press.shared.ui.Presenter

class SyncPreferencesPresenter(
  private val http: HttpClient,
  private val syncer: Syncer,
  private val schedulers: Schedulers,
  private val authToken: (GitHost) -> Setting<GitHostAuthToken>
) : Presenter<SyncPreferencesEvent, SyncPreferencesUiModel, SyncPreferencesUiEffect>() {

  override fun defaultUiModel(): SyncPreferencesUiModel {
    return SyncDisabled(availableGitHosts = emptyList())
  }

  override fun uiModels(): ObservableWrapper<SyncPreferencesUiModel> {
    val models = syncer.status()
        .map { status ->
          when (status) {
            Disabled -> SyncDisabled(
                availableGitHosts = GitHost.values().toList()
            )
            else -> SyncEnabled(
                setupInfo = "Syncing is enabled",
                status = "Last synced x seconds ago."
            )
          }
        }.distinctUntilChanged()

    return models
        .mergeWith(handleDisableSyncClicks())
        .wrap()
  }

  private fun handleDisableSyncClicks(): Observable<SyncPreferencesUiModel> {
    return viewEvents().ofType<DisableSyncClicked>()
        .observeOn(schedulers.io)
        .flatMapCompletable { syncer.disable() }
        .asObservable()
  }

  override fun uiEffects(): ObservableWrapper<SyncPreferencesUiEffect> {
    return viewEvents()
        .ofType<SetupHostClicked>()
        .map { (host) ->
          authToken(host).set(null)
          val service = host.service(http)
          OpenUrl(service.generateAuthUrl(host.deepLink()))
        }
        .wrap()
  }
}
