package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.completable.completableFromFunction
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
import me.saket.press.shared.sync.FailureKind
import me.saket.press.shared.sync.FailureKind.AddingDeployKey
import me.saket.press.shared.sync.FailureKind.Authorization
import me.saket.press.shared.sync.FailureKind.FetchingRepos
import me.saket.press.shared.sync.GitHostAuthEvent
import me.saket.press.shared.sync.GitHostAuthEvent.GitRepositoryClicked
import me.saket.press.shared.sync.GitHostAuthEvent.RetryClicked
import me.saket.press.shared.sync.GitHostAuthUiEffect
import me.saket.press.shared.sync.GitHostAuthUiEffect.OpenAuthorizationUrl
import me.saket.press.shared.sync.GitHostAuthUiModel
import me.saket.press.shared.sync.GitHostAuthUiModel.SelectRepo
import me.saket.press.shared.sync.GitHostAuthUiModel.ShowFailure
import me.saket.press.shared.sync.GitHostAuthUiModel.ShowProgress
import me.saket.press.shared.sync.git.GitHost.GITHUB
import me.saket.press.shared.sync.git.service.GitHostService
import me.saket.press.shared.ui.Presenter

@OptIn(ExperimentalListener::class)
class GitHostAuthPresenter(
  httpClient: HttpClient,
  authToken: (GitHost) -> Setting<GitHostAuthToken>,
  private val syncer: GitSyncer,
  private val syncerConfig: Setting<GitSyncerConfig>,
  private val deepLinks: DeepLinks,
  private val ioScheduler: Scheduler
) : Presenter<GitHostAuthEvent, GitHostAuthUiModel, GitHostAuthUiEffect>() {

  private val gitHost: GitHost = GITHUB   // todo: get from View
  private val authToken: Setting<GitHostAuthToken> = authToken(gitHost)
  private val gitHostService: GitHostService = gitHost.service(httpClient)

  override fun defaultUiModel() = ShowProgress

  override fun uiModels(): ObservableWrapper<GitHostAuthUiModel> {
    return viewEvents().publish { events ->
      merge(
          completeAuthorization(events),
          populateRepositories(events),
          selectRepository(events)
      )
    }.wrap()
  }

  override fun uiEffects(): ObservableWrapper<GitHostAuthUiEffect> {
    return requestAuthorization().wrap()
  }

  private fun requestAuthorization(): Observable<OpenAuthorizationUrl> {
    return authToken.listen()
        .take(1)
        .filterNull()
        .map { OpenAuthorizationUrl(gitHostService.generateAuthUrl()) }
  }

  private fun completeAuthorization(events: Observable<GitHostAuthEvent>): Observable<GitHostAuthUiModel> {
    return deepLinks.listen()
        .filter { it.url.startsWith("intent://press/authorization-granted") }
        .repeatOnRetry(events, kind = Authorization)
        .switchMap { link ->
          gitHostService.completeAuth(link.url)
              .asObservable()
              .consumeOnNext<GitHostAuthToken, GitHostAuthUiModel> {
                authToken.set(it)
              }
              .onErrorReturnValue(ShowFailure(kind = Authorization))
        }
  }

  private fun populateRepositories(events: Observable<GitHostAuthEvent>): Observable<GitHostAuthUiModel> {
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

  private fun selectRepository(events: Observable<GitHostAuthEvent>): Observable<GitHostAuthUiModel> {
    return events.ofType<GitRepositoryClicked>()
        .map { it.repo }
        .repeatOnRetry(events, kind = AddingDeployKey)
        .switchMap { repo ->
          val sshKey = SshKeygen.generateRsa(comment = "(Created by Press)")
          gitHostService
              .addDeployKey(token = authToken.get()!!, repository = repo, key = sshKey)
              .andThen(completableFromFunction {
                println("TODO: Close screen")
                authToken.set(null)
                syncerConfig.set(GitSyncerConfig(remote = repo, sshKey = sshKey.privateKey))
                syncer.sync()
              })
              .asObservable<GitHostAuthUiModel>()
              .onErrorReturnValue(ShowFailure(kind = AddingDeployKey))
              .startWithValue(ShowProgress)
        }
  }

  private fun <T> Observable<T>.repeatOnRetry(
    events: Observable<GitHostAuthEvent>,
    kind: FailureKind
  ) = repeatWhen(events.ofType<RetryClicked>().filter { it.failure == kind })
}
