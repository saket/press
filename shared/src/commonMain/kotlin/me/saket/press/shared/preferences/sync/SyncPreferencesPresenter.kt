package me.saket.press.shared.preferences.sync

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.wrap
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.soywiz.klock.minutes
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.preferences.Setting
import me.saket.press.shared.preferences.sync.SyncPreferencesEvent.DisableSyncClicked
import me.saket.press.shared.preferences.sync.SyncPreferencesEvent.SetupHostClicked
import me.saket.press.shared.preferences.sync.SyncPreferencesUiModel.SyncDisabled
import me.saket.press.shared.preferences.sync.SyncPreferencesUiModel.SyncEnabled
import me.saket.press.shared.syncer.LastSyncedAt
import me.saket.press.shared.syncer.Syncer
import me.saket.press.shared.syncer.Syncer.Status.Disabled
import me.saket.press.shared.syncer.Syncer.Status.Enabled
import me.saket.press.shared.syncer.Syncer.Status.LastOp.Failed
import me.saket.press.shared.syncer.Syncer.Status.LastOp.Idle
import me.saket.press.shared.syncer.Syncer.Status.LastOp.InFlight
import me.saket.press.shared.syncer.git.GitHost
import me.saket.press.shared.syncer.git.GitHostAuthToken
import me.saket.press.shared.preferences.sync.setup.GitRepositoryCache
import me.saket.press.shared.syncer.git.service.GitHostService
import me.saket.press.shared.time.Clock
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.util.format

class SyncPreferencesPresenter(
  private val args: Args,
  private val syncer: Syncer,
  private val gitHostService: GitHostService.Factory,
  private val schedulers: Schedulers,
  private val authToken: (GitHost) -> Setting<GitHostAuthToken>,
  private val clock: Clock,
  private val strings: Strings,
  private val cachedRepos: GitRepositoryCache
) : Presenter<SyncPreferencesEvent, SyncPreferencesUiModel>() {

  override fun defaultUiModel(): SyncPreferencesUiModel {
    return SyncDisabled(availableGitHosts = emptyList())
  }

  @Suppress("NAME_SHADOWING")
  override fun models(): ObservableWrapper<SyncPreferencesUiModel> {
    val models = syncer.status()
      .map { status ->
        when (status) {
          is Disabled -> SyncDisabled(
            availableGitHosts = GitHost.values().toList()
          )
          is Enabled -> {
            // InFlight:
            //   "Syncing..."
            // Idle:
            //   - has timestamp: "Synced 30m ago"
            //   - no timestamp:  "Waiting to sync"
            // Failed:
            //   - has timestamp: "Synced 30m ago. Last attempt failed, will retry?"
            //   - no timestamp: "Last attempt failed, will retry?"
            val lastSynced = status.lastSyncedAt
            val statusText = when (status.lastOp) {
              InFlight -> strings.sync.status_in_flight
              Idle -> lastSynced?.relativeTimestamp() ?: strings.sync.status_idle_never_synced
              Failed -> (lastSynced?.relativeTimestamp()?.plus(". ") ?: "") + strings.sync.status_failed
            }
            SyncEnabled(
              gitHost = status.syncingWith.host,
              remoteName = status.syncingWith.ownerAndName,
              remoteUrl = status.syncingWith.url,
              status = statusText
            )
          }
        }
      }.distinctUntilChanged()

    return merge(
      models,
      handleDisableSyncClicks(),
      handleOpenAuthClicks()
    ).wrap()
  }

  private fun LastSyncedAt.relativeTimestamp(): String {
    val timePassed = clock.nowUtc() - value
    return strings.sync.status_synced_x_ago.format(
      when {
        timePassed < 1.minutes -> strings.sync.timestamp_now
        timePassed < 1.hours -> strings.sync.timestamp_minutes.format(timePassed.minutes.toInt())
        timePassed < 1.days -> strings.sync.timestamp_hours.format(timePassed.hours.toInt())
        else -> strings.sync.timestamp_a_while_ago
      }
    )
  }

  private fun handleDisableSyncClicks(): Observable<SyncPreferencesUiModel> {
    return viewEvents()
      .ofType<DisableSyncClicked>()
      .observeOn(schedulers.io)
      .consumeOnNext { syncer.disable() }
  }

  private fun handleOpenAuthClicks(): Observable<SyncPreferencesUiModel> {
    return viewEvents()
      .ofType<SetupHostClicked>()
      .consumeOnNext { (host) ->
        cachedRepos.set(null)
        authToken(host).set(null)
        val service = gitHostService.create(host)
        args.navigator.intentLauncher().openUrl(service.generateAuthUrl(host.deepLink()))
      }
  }

  fun interface Factory {
    fun create(args: Args): SyncPreferencesPresenter
  }

  data class Args(
    val navigator: Navigator
  )
}
