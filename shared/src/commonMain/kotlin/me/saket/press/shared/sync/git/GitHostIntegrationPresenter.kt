package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.completable.asSingle
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.doOnBeforeError
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOfNever
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.onErrorResumeNext
import com.badoo.reaktive.observable.onErrorReturnValue
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.repeatWhen
import com.badoo.reaktive.observable.startWithValue
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.observable.wrap
import com.badoo.reaktive.single.asObservable
import com.badoo.reaktive.single.zip
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import com.russhwolf.settings.ExperimentalListener
import io.ktor.client.HttpClient
import me.saket.kgit.GitIdentity
import me.saket.kgit.SshKeyPair
import me.saket.kgit.SshKeygen
import me.saket.kgit.generateRsa
import me.saket.press.PressDatabase
import me.saket.press.shared.rx.combineLatestWith
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.rx.filterNotNull
import me.saket.press.shared.rx.filterNull
import me.saket.press.shared.rx.repeatItemWhen
import me.saket.press.shared.rx.zip
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.SyncCoordinator
import me.saket.press.shared.sync.git.FailureKind.AddingDeployKey
import me.saket.press.shared.sync.git.FailureKind.Authorization
import me.saket.press.shared.sync.git.FailureKind.FetchingRepos
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.GitRepositoryClicked
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.RetryClicked
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.SearchTextChanged
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.SelectRepo
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowFailure
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowProgress
import me.saket.press.shared.sync.git.service.GitHostService
import me.saket.press.shared.sync.git.service.GitRepositoryInfo
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.ui.ScreenKey.Close

@OptIn(ExperimentalListener::class)
class GitHostIntegrationPresenter(
  private val args: Args,
  httpClient: HttpClient,
  authToken: (GitHost) -> Setting<GitHostAuthToken>,
  gitHostService: (GitHost, HttpClient) -> GitHostService = { host, http -> host.service(http) },
  private val deviceInfo: DeviceInfo,
  private val database: PressDatabase,
  private val cachedRepos: GitRepositoryCache,
  private val syncCoordinator: SyncCoordinator,
) : Presenter<GitHostIntegrationEvent, GitHostIntegrationUiModel, Nothing>() {

  private val gitHost = GitHost.readHostFromDeepLink(args.deepLink)
  private val authToken: Setting<GitHostAuthToken> = authToken(gitHost)
  private val gitHostService: GitHostService = gitHostService(gitHost, httpClient)

  override fun defaultUiModel() = ShowProgress

  override fun uiModels(): ObservableWrapper<GitHostIntegrationUiModel> {
    return viewEvents().publish { events ->
      merge(
          completeAuth(events),
          fetchRepositories(events),
          displayRepositories(events),
          selectRepository(events)
      )
    }.wrap()
  }

  private fun completeAuth(
    events: Observable<GitHostIntegrationEvent>
  ): Observable<GitHostIntegrationUiModel> {
    return cachedRepos.listen()
        .take(1)
        .filterNull()
        .switchMap {
          gitHostService.completeAuth(args.deepLink)
              .asObservable()
              .repeatOnRetry(events, kind = Authorization)
              .consumeOnNext<GitHostAuthToken, GitHostIntegrationUiModel> {
                authToken.set(it)
              }
              .doOnBeforeError { e -> e.printStackTrace() }
              .onErrorReturnValue(ShowFailure(kind = Authorization))
              .startWithValue(defaultUiModel())
        }
  }

  private fun fetchRepositories(
    events: Observable<GitHostIntegrationEvent>
  ): Observable<GitHostIntegrationUiModel> {
    return zip(
        authToken.listen().filterNotNull(),
        cachedRepos.listen().filterNull()
    )
        .take(1)
        .switchMap { (token) ->
          gitHostService.fetchUserRepos(token)
              .asObservable()
              .repeatOnRetry(events, kind = FetchingRepos)
        }
        .publish { userRepos ->
          merge(
              userRepos.ignoreErrors().consumeOnNext { cachedRepos.set(it) },
              userRepos
                  .doOnBeforeError { e -> e.printStackTrace() }
                  .onErrorReturnValue(ShowFailure(kind = FetchingRepos))
          )
        }
        .ofType()
  }

  private fun displayRepositories(
    events: Observable<GitHostIntegrationEvent>
  ): Observable<GitHostIntegrationUiModel> {
    val searchEvents = events
        .ofType<SearchTextChanged>()
        .map { it.text }

    return cachedRepos.listen()
        .filterNotNull()
        .combineLatestWith(searchEvents)
        .map { (repos, searchText) -> SelectRepo(repos.toUiModels(searchText)) }
        .distinctUntilChanged()
  }

  private fun selectRepository(
    events: Observable<GitHostIntegrationEvent>
  ): Observable<GitHostIntegrationUiModel> {
    return events.ofType<GitRepositoryClicked>()
        .map { it.repo }
        .repeatItemOnRetry(events, kind = AddingDeployKey)
        .switchMap { repo ->
          val token = authToken.get()!!
          val deployKey = GitHostService.DeployKey(
            title = "Press (${deviceInfo.deviceName()})",
            key = SshKeygen.generateRsa(comment = "(Created by Press)")
          )
          zip(
            gitHostService.addDeployKey(token, repo, deployKey).asSingle(Unit),
            gitHostService.fetchUser(token)
          ) { _, user -> user }
              .asObservable()
              .flatMapCompletable { user -> completeSetup(repo, deployKey, user) }
              .asObservable<GitHostIntegrationUiModel>()
              .doOnBeforeError { e -> e.printStackTrace() }
              .onErrorReturnValue(ShowFailure(kind = AddingDeployKey))
              .startWithValue(ShowProgress)
        }
  }

  private fun completeSetup(
    repo: GitRepositoryInfo,
    deployKey: GitHostService.DeployKey,
    user: GitIdentity
  ): Completable {
    return completableFromFunction {
      authToken.set(null)
      database.folderSyncConfigQueries.insert(
          folder = null,
          remote = GitSyncerConfig(repo, deployKey.key.privateKey, user)
      )
      syncCoordinator.trigger()
      args.navigator.lfg(Close)
    }
  }

  private fun <T> Observable<T>.repeatItemOnRetry(
    events: Observable<GitHostIntegrationEvent>,
    kind: FailureKind
  ) = repeatItemWhen(events.ofType<RetryClicked>().filter { it.failure == kind })

  private fun <T> Observable<T>.repeatOnRetry(
    events: Observable<GitHostIntegrationEvent>,
    kind: FailureKind
  ) = repeatWhen { events.ofType<RetryClicked>().filter { it.failure == kind }.firstOrComplete() }

  interface Factory {
    fun create(args: Args): GitHostIntegrationPresenter
  }

  data class Args(
    val deepLink: String,
    val navigator: Navigator
  )
}

private fun <T> Observable<T>.ignoreErrors(): Observable<T> {
  return this.onErrorResumeNext(observableOfNever())
}

private fun List<GitRepositoryInfo>.toUiModels(searchText: String): List<RepoUiModel> {
  val filtered = when {
    searchText.isBlank() -> this
    else -> filter {
      it.owner.contains(searchText, ignoreCase = true) || it.name.contains(searchText, ignoreCase = true)
    }
  }
  return filtered.map {
    RepoUiModel(
        id = it.ownerAndName,
        owner = HighlightedText.from(it.owner, searchText),
        name = HighlightedText.from(it.name, searchText),
        repo = it
    )
  }
}

interface GitRepositoryCache {
  fun listen(): Observable<List<GitRepositoryInfo>?>
  fun set(repos: List<GitRepositoryInfo>?)

  class InMemory : GitRepositoryCache {
    private val cache = BehaviorSubject<List<GitRepositoryInfo>?>(null)
    override fun listen() = cache
    override fun set(repos: List<GitRepositoryInfo>?) = cache.onNext(repos)
  }
}
