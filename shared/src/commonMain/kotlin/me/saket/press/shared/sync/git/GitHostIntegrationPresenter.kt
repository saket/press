package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.completable.asSingle
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.doOnBeforeError
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.onErrorReturnValue
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.startWithValue
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.observable.wrap
import com.badoo.reaktive.single.asObservable
import com.badoo.reaktive.single.zip
import com.russhwolf.settings.ExperimentalListener
import io.ktor.client.HttpClient
import me.saket.kgit.SshKeyPair
import me.saket.kgit.SshKeygen
import me.saket.kgit.generateRsa
import me.saket.press.shared.rx.combineLatestWith
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.rx.filterNotNull
import me.saket.press.shared.rx.repeatWhen
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
import me.saket.kgit.GitIdentity
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.ui.ScreenKey.Close

@OptIn(ExperimentalListener::class)
class GitHostIntegrationPresenter(
  private val args: Args,
  httpClient: HttpClient,
  authToken: (GitHost) -> Setting<GitHostAuthToken>,
  private val syncCoordinator: SyncCoordinator,
  private val syncerConfig: Setting<GitSyncerConfig>
) : Presenter<GitHostIntegrationEvent, GitHostIntegrationUiModel, Nothing>() {

  private val gitHost = GitHost.readHostFromDeepLink(args.deepLink)
  private val authToken: Setting<GitHostAuthToken> = authToken(gitHost)
  private val gitHostService: GitHostService = gitHost.service(httpClient)

  override fun defaultUiModel() = ShowProgress

  override fun uiModels(): ObservableWrapper<GitHostIntegrationUiModel> {
    return viewEvents().publish { events ->
      merge(
          completeAuth(events),
          populateRepositories(events),
          selectRepository(events)
      )
    }.wrap()
  }

  private fun completeAuth(events: Observable<GitHostIntegrationEvent>): Observable<GitHostIntegrationUiModel> {
    return observableOf(Unit)
        .repeatOnRetry(events, kind = Authorization)
        .switchMap {
          gitHostService.completeAuth(args.deepLink)
              .asObservable()
              .consumeOnNext<GitHostAuthToken, GitHostIntegrationUiModel> {
                authToken.set(it)
              }
              .onErrorReturnValue(ShowFailure(kind = Authorization))
              .startWithValue(defaultUiModel())
        }
  }

  private fun populateRepositories(events: Observable<GitHostIntegrationEvent>): Observable<GitHostIntegrationUiModel> {
    val searchEvents = events
        .ofType<SearchTextChanged>()
        .map { it.text }
        .startWithValue("")

    return authToken.listen()
        .filterNotNull()
        .take(1)
        .repeatOnRetry(events, kind = FetchingRepos)
        .switchMap { token ->
          gitHostService.fetchUserRepos(token).asObservable()
              .combineLatestWith(searchEvents)
              .map { (repos, searchText) -> SelectRepo(repos.toUiModels(searchText)) }
              .doOnBeforeError { e -> e.printStackTrace() }
              .onErrorReturnValue(ShowFailure(kind = FetchingRepos))
              .startWithValue(defaultUiModel())
        }
        .distinctUntilChanged()
  }

  private fun selectRepository(events: Observable<GitHostIntegrationEvent>): Observable<GitHostIntegrationUiModel> {
    return events.ofType<GitRepositoryClicked>()
        .map { it.repo }
        .repeatOnRetry(events, kind = AddingDeployKey)
        .switchMap { repo ->
          val sshKeys = SshKeygen.generateRsa(comment = "(Created by Press)")
          val token = authToken.get()!!
          zip(
            gitHostService.addDeployKey(token, repo, sshKeys).asSingle(Unit),
            gitHostService.fetchUser(token)
          ) { _, user -> user }
              .asObservable()
              .flatMapCompletable { user -> completeSetup(repo, sshKeys, user) }
              .asObservable<GitHostIntegrationUiModel>()
              .doOnBeforeError { e -> e.printStackTrace() }
              .onErrorReturnValue(ShowFailure(kind = AddingDeployKey))
              .startWithValue(ShowProgress)
        }
  }

  private fun completeSetup(repo: GitRepositoryInfo, sshKeys: SshKeyPair, user: GitIdentity) =
    completableFromFunction {
      authToken.set(null)
      syncerConfig.set(GitSyncerConfig(repo, sshKeys.privateKey, user))
      syncCoordinator.trigger()
      args.navigator.lfg(Close)
    }

  private fun <T> Observable<T>.repeatOnRetry(
    events: Observable<GitHostIntegrationEvent>,
    kind: FailureKind
  ) = repeatWhen(events.ofType<RetryClicked>().filter { it.failure == kind })

  interface Factory {
    fun create(args: Args): GitHostIntegrationPresenter
  }

  data class Args(
    val deepLink: String,
    val navigator: Navigator
  )
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
