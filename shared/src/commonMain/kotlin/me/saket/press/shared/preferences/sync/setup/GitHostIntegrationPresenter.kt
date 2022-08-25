package me.saket.press.shared.preferences.sync.setup

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.doOnBeforeError
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOfNever
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.onErrorResumeNext
import com.badoo.reaktive.observable.onErrorReturnValue
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.startWithValue
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.observable.withLatestFrom
import com.badoo.reaktive.observable.wrap
import com.badoo.reaktive.single.asObservable
import com.badoo.reaktive.single.zip
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import me.saket.kgit.GitIdentity
import me.saket.kgit.SshKeygen
import me.saket.press.PressDatabase
import me.saket.press.shared.rx.combineLatestWith
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.rx.filterNotNull
import me.saket.press.shared.rx.filterNull
import me.saket.press.shared.rx.mergeWith
import me.saket.press.shared.rx.repeatItemWhen
import me.saket.press.shared.rx.zip
import me.saket.press.shared.preferences.Setting
import me.saket.press.shared.syncer.SyncCoordinator
import me.saket.press.shared.preferences.sync.setup.FailureKind.AddingDeployKey
import me.saket.press.shared.preferences.sync.setup.FailureKind.Authorization
import me.saket.press.shared.preferences.sync.setup.FailureKind.FetchingRepos
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationEvent.CreateNewGitRepoClicked
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationEvent.GitRepositoryClicked
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationEvent.RetryClicked
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationEvent.SearchTextChanged
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationUiModel.SelectRepo
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationUiModel.ShowFailure
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationUiModel.ShowProgress
import me.saket.press.shared.syncer.git.DeviceInfo
import me.saket.press.shared.syncer.git.GitHost
import me.saket.press.shared.syncer.git.GitHostAuthToken
import me.saket.press.shared.syncer.git.GitRemoteAndAuth
import me.saket.press.shared.syncer.git.service.GitHostService
import me.saket.press.shared.syncer.git.service.GitRepositoryInfo
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.ui.ScreenResults
import me.saket.press.shared.ui.highlight

class GitHostIntegrationPresenter(
  private val args: Args,
  authToken: (GitHost) -> Setting<GitHostAuthToken>,
  gitHostService: GitHostService.Factory,
  private val userSetting: Setting<GitIdentity>,
  private val deviceInfo: DeviceInfo,
  private val database: PressDatabase,
  private val cachedRepos: GitRepositoryCache,
  private val syncCoordinator: SyncCoordinator,
  private val sshKeygen: SshKeygen,
  private val screenResults: ScreenResults
) : Presenter<GitHostIntegrationEvent, GitHostIntegrationUiModel>() {

  private val gitHost = GitHost.readHostFromDeepLink(args.deepLink)
  private val authToken: Setting<GitHostAuthToken> = authToken(gitHost)
  private val gitHostService: GitHostService = gitHostService.create(gitHost)

  override fun defaultUiModel() = ShowProgress

  override fun models(): ObservableWrapper<GitHostIntegrationUiModel> {
    return viewEvents().publish { events ->
      merge(
        completeAuth(events),
        fetchRepositories(events),
        displayRepositories(events),
        showNewGitRepoScreen(events),
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
      .repeatItemOnRetry(events, kind = Authorization)
      .switchMap {
        gitHostService.completeAuth(args.deepLink)
          .asObservable()
          .consumeOnNext<GitHostAuthToken, GitHostIntegrationUiModel> {
            authToken.set(it)
          }
          .doOnBeforeError { e -> e.printStackTrace() }
          .onErrorReturnValue(ShowFailure(kind = Authorization))
          .startWithValue(ShowProgress)
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
      .repeatItemOnRetry(events, kind = FetchingRepos)
      .switchMap { (token) ->
        zip(
          gitHostService.fetchUser(token),
          gitHostService.fetchUserRepos(token),
          ::Pair
        )
          .asObservable()
          .publish { networkCalls ->
            merge(
              networkCalls.ignoreErrors()
                .consumeOnNext { (user, repos) ->
                  userSetting.set(user)
                  cachedRepos.set(repos)
                },
              networkCalls
                .doOnBeforeError { e -> e.printStackTrace() }
                .onErrorReturnValue(ShowFailure(kind = FetchingRepos))
                .startWithValue(ShowProgress)
            )
          }
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

  private fun showNewGitRepoScreen(
    events: Observable<GitHostIntegrationEvent>
  ): Observable<GitHostIntegrationUiModel> {
    val searchTexts = events
      .ofType<SearchTextChanged>()
      .map { it.text }

    return events.ofType<CreateNewGitRepoClicked>()
      .withLatestFrom(searchTexts, ::Pair)
      .consumeOnNext { (_, searchText) ->
        args.navigator.lfg(
          NewGitRepositoryScreenKey(
            username = userSetting.get()!!.name,
            gitHost = gitHost,
            preFilledRepoName = searchText
          )
        )
      }
  }

  private fun selectRepository(
    events: Observable<GitHostIntegrationEvent>
  ): Observable<GitHostIntegrationUiModel> {
    val repoSelections = events.ofType<GitRepositoryClicked>()
      .map { it.repo }
      .mergeWith(
        screenResults
          .listen<NewGitRepositoryCreatedResult>()
          .map { it.repo }
      )

    return repoSelections
      .repeatItemOnRetry(events, kind = AddingDeployKey)
      .switchMap { repo ->
        val token = authToken.get()!!
        val deployKey = GitHostService.DeployKey(
          title = "Press (${deviceInfo.deviceName()})",
          key = sshKeygen.generateEcdsa(comment = "(Created by Press)")
        )
        gitHostService.addDeployKey(token, repo, deployKey)
          .andThen(completeSetup(repo, deployKey, userSetting.get()!!))
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
      userSetting.set(null)
      database.folderSyncConfigQueries.save(
        remote = GitRemoteAndAuth(repo, deployKey.key.privateKey, user)
      )
      syncCoordinator.trigger()
      args.navigator.goBack()
    }
  }

  private fun <T> Observable<T>.repeatItemOnRetry(
    events: Observable<GitHostIntegrationEvent>,
    kind: FailureKind
  ) = repeatItemWhen(events.ofType<RetryClicked>().filter { it.failure == kind })

  fun interface Factory {
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
      owner = it.owner.highlight(searchText),
      name = it.name.highlight(searchText),
      repo = it
    )
  }
}

interface GitRepositoryCache {
  fun listen(): Observable<List<GitRepositoryInfo>?>
  fun set(repos: List<GitRepositoryInfo>?)

  class InMemory : GitRepositoryCache {
    private val cache = BehaviorSubject<List<GitRepositoryInfo>?>(null)
    override fun listen(): Observable<List<GitRepositoryInfo>?> = cache
    override fun set(repos: List<GitRepositoryInfo>?) = cache.onNext(repos)
  }
}
