package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.completable.doOnAfterComplete
import com.badoo.reaktive.completable.onErrorComplete
import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.onErrorReturnValue
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.startWithValue
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.switchMapSingle
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.observable.wrap
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.single.asObservable
import com.badoo.reaktive.single.map
import com.badoo.reaktive.single.onErrorReturnValue
import com.russhwolf.settings.ExperimentalListener
import io.ktor.client.HttpClient
import me.saket.kgit.SshKeygen
import me.saket.kgit.generateRsa
import me.saket.press.shared.DeepLinks
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.rx.filterNotNull
import me.saket.press.shared.rx.filterNull
import me.saket.press.shared.rx.repeatWhen
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.git.FailureKind.AddingDeployKey
import me.saket.press.shared.sync.git.FailureKind.Authorization
import me.saket.press.shared.sync.git.FailureKind.FetchingRepos
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.GitRepositoryClicked
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.RetryClicked
import me.saket.press.shared.sync.git.GitHostIntegrationUiEffect.OpenAuthorizationUrl
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.SelectRepo
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowFailure
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowProgress
import me.saket.press.shared.sync.git.service.GitHostService
import me.saket.press.shared.ui.Presenter

@OptIn(ExperimentalListener::class)
class GitHostIntegrationPresenter(
  args: Args,
  httpClient: HttpClient,
  authToken: (GitHost) -> Setting<GitHostAuthToken>,
  private val syncer: GitSyncer,
  private val syncerConfig: Setting<GitSyncerConfig>,
  private val deepLinks: DeepLinks,
  private val ioScheduler: Scheduler
) : Presenter<GitHostIntegrationEvent, GitHostIntegrationUiModel, GitHostIntegrationUiEffect>() {

  private val authToken: Setting<GitHostAuthToken> = authToken(args.host)
  private val gitHostService: GitHostService = args.host.service(httpClient)

  override fun defaultUiModel() = ShowProgress

  override fun uiModels(): ObservableWrapper<GitHostIntegrationUiModel> {
    return viewEvents().publish { events ->
      merge(
          completeAuthorization(events),
          populateRepositories(events),
          selectRepository(events)
      )
    }.wrap()
  }

  override fun uiEffects(): ObservableWrapper<GitHostIntegrationUiEffect> {
    return requestAuthorization().wrap()
  }

  private fun requestAuthorization(): Observable<OpenAuthorizationUrl> {
    return authToken.listen()
        .take(1)
        .filterNull()
        .map { OpenAuthorizationUrl(gitHostService.generateAuthUrl()) }
  }

  private fun completeAuthorization(events: Observable<GitHostIntegrationEvent>): Observable<GitHostIntegrationUiModel> {
    return deepLinks.listen()
        .filter { it.url.startsWith("intent://press/authorization-granted") }
        .repeatOnRetry(events, kind = Authorization)
        .switchMap { link ->
          gitHostService.completeAuth(link.url)
              .asObservable()
              .consumeOnNext<GitHostAuthToken, GitHostIntegrationUiModel> {
                authToken.set(it)
              }
              .onErrorReturnValue(ShowFailure(kind = Authorization))
        }
  }

  private fun populateRepositories(events: Observable<GitHostIntegrationEvent>): Observable<GitHostIntegrationUiModel> {
    return authToken.listen()
        .filterNotNull()
        .take(1)
        .repeatOnRetry(events, kind = FetchingRepos)
        .switchMapSingle { token ->
          gitHostService.fetchUserRepos(token)
              .map { repos -> SelectRepo(repos) }
              .onErrorReturnValue(ShowFailure(kind = FetchingRepos))
        }
  }

  private fun selectRepository(events: Observable<GitHostIntegrationEvent>): Observable<GitHostIntegrationUiModel> {
    return events.ofType<GitRepositoryClicked>()
        .map { it.repo }
        .repeatOnRetry(events, kind = AddingDeployKey)
        .switchMap { repo ->
          val sshKey = SshKeygen.generateRsa(comment = "(Created by Press)")
          gitHostService
              .addDeployKey(token = authToken.get()!!, repository = repo, key = sshKey)
              .subscribeOn(ioScheduler)
              .andThen(completableFromFunction {
                println("TODO: Close screen")
                authToken.set(null)
                syncerConfig.set(GitSyncerConfig(remote = repo, sshKey = sshKey.privateKey))
              })
              .doOnAfterComplete { syncNotesAsync() }
              .asObservable<GitHostIntegrationUiModel>()
              .onErrorReturnValue(ShowFailure(kind = AddingDeployKey))
              .startWithValue(ShowProgress)
        }
  }

  private fun syncNotesAsync() {
    syncer.sync()
        .subscribeOn(ioScheduler)
        .onErrorComplete()
        .subscribe()
  }

  private fun <T> Observable<T>.repeatOnRetry(
    events: Observable<GitHostIntegrationEvent>,
    kind: FailureKind
  ) = repeatWhen(events.ofType<RetryClicked>().filter { it.failure == kind })

  interface Factory {
    fun create(args: Args): GitHostIntegrationPresenter
  }

  data class Args(val host: GitHost)
}
