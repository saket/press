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
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.soywiz.klock.minutes
import io.ktor.client.HttpClient
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.rx.mergeWith
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.SyncPreferencesEvent.DisableSyncClicked
import me.saket.press.shared.sync.SyncPreferencesEvent.SetupHostClicked
import me.saket.press.shared.sync.SyncPreferencesUiEffect.OpenUrl
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncDisabled
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncEnabled
import me.saket.press.shared.sync.Syncer.Status.Disabled
import me.saket.press.shared.sync.Syncer.Status.Failed
import me.saket.press.shared.sync.Syncer.Status.Idle
import me.saket.press.shared.sync.Syncer.Status.InFlight
import me.saket.press.shared.sync.git.GitHost
import me.saket.press.shared.sync.git.GitHostAuthToken
import me.saket.press.shared.time.Clock
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.util.format

class SyncPreferencesPresenter(
  private val http: HttpClient,
  private val syncer: Syncer,
  private val schedulers: Schedulers,
  private val authToken: (GitHost) -> Setting<GitHostAuthToken>,
  private val clock: Clock,
  private val strings: Strings
) : Presenter<SyncPreferencesEvent, SyncPreferencesUiModel, SyncPreferencesUiEffect>() {

  override fun defaultUiModel(): SyncPreferencesUiModel {
    return SyncDisabled(availableGitHosts = emptyList())
  }

  override fun uiModels(): ObservableWrapper<SyncPreferencesUiModel> {
    val models = syncer.status()
        .map { status ->
          when (status) {
            is Disabled -> SyncDisabled(
                availableGitHosts = GitHost.values().toList()
            )
            else -> {
              val statusText = when (status) {
                is Disabled -> error("can't")
                is InFlight -> strings.sync.status_in_flight
                is Failed -> strings.sync.status_failed
                is Idle -> relativeSyncTimestamp(status.lastSyncedAt)
              }
              SyncEnabled(
                  setupInfo = "Syncing is enabled",
                  status = statusText
              )
            }
          }
        }.distinctUntilChanged()

    return models
        .mergeWith(handleDisableSyncClicks())
        .wrap()
  }

  private fun relativeSyncTimestamp(lastSyncedAt: DateTime?): String {
    return if (lastSyncedAt == null) {
      strings.sync.status_last_synced_never

    } else {
      val timePassed = clock.nowUtc() - lastSyncedAt
      strings.sync.status_last_synced_x_ago.format(
          when {
            timePassed < 1.minutes -> strings.sync.timestamp_now
            timePassed < 1.hours -> strings.sync.timestamp_minutes.format(timePassed.minutes)
            timePassed < 1.days -> strings.sync.timestamp_hours.format(timePassed.hours)
            else -> strings.sync.timestamp_a_while_ago
          }
      )
    }
  }

  private fun handleDisableSyncClicks(): Observable<SyncPreferencesUiModel> {
    return viewEvents().ofType<DisableSyncClicked>()
        .observeOn(schedulers.io)
        .consumeOnNext { syncer.disable() }
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
