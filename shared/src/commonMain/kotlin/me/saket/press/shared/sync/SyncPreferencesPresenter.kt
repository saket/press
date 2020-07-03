package me.saket.press.shared.sync

import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.wrap
import io.ktor.client.HttpClient
import me.saket.press.shared.sync.SyncPreferencesEvent.SetupHostClicked
import me.saket.press.shared.sync.SyncPreferencesUiEffect.OpenUrl
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncingDisabled
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncingEnabled
import me.saket.press.shared.sync.Syncer.Status.Disabled
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.ui.Presenter

class SyncPreferencesPresenter(
  private val http: HttpClient,
  private val syncer: Syncer
) : Presenter<SyncPreferencesEvent, SyncPreferencesUiModel, SyncPreferencesUiEffect>() {

  override fun defaultUiModel(): SyncPreferencesUiModel {
    return SyncingDisabled(availableGitHosts = emptyList())
  }

  override fun uiModels(): ObservableWrapper<SyncPreferencesUiModel> {
    return syncer.status()
        .map { status ->
          when (status) {
            Disabled -> SyncingDisabled(
                availableGitHosts = GitHost.values().toList()
            )
            else -> SyncingEnabled(
                setupInfo = "Syncing is enabled",
                status = "Last synced x seconds ago."
            )
          }
        }.distinctUntilChanged()
        .wrap()
  }

  override fun uiEffects(): ObservableWrapper<SyncPreferencesUiEffect> {
    return viewEvents()
        .ofType<SetupHostClicked>()
        .map { (host) ->
          val service = host.service(http)
          OpenUrl(service.generateAuthUrl(host.deepLink()))
        }
        .wrap()
  }
}
