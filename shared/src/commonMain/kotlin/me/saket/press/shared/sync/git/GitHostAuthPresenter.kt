package me.saket.press.shared.sync.git

import co.touchlab.stately.concurrency.AtomicBoolean
import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.doOnBeforeNext
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.onErrorReturnValue
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.switchMapCompletable
import com.badoo.reaktive.observable.switchMapSingle
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.observable.wrap
import com.badoo.reaktive.single.asObservable
import com.badoo.reaktive.single.doOnBeforeSuccess
import com.badoo.reaktive.single.map
import com.badoo.reaktive.single.onErrorReturn
import com.russhwolf.settings.ExperimentalListener
import com.russhwolf.settings.ObservableSettings
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import me.saket.press.shared.DeepLinks
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.rx.filterNotNull
import me.saket.press.shared.rx.filterNull
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.GitHostAuthEvent
import me.saket.press.shared.sync.GitHostAuthEvent.GitRepositoryClicked
import me.saket.press.shared.sync.GitHostAuthUiEffect
import me.saket.press.shared.sync.GitHostAuthUiEffect.OpenAuthorizationUrl
import me.saket.press.shared.sync.GitHostAuthUiModel
import me.saket.press.shared.sync.GitHostAuthUiModel.FullscreenError
import me.saket.press.shared.sync.GitHostAuthUiModel.Loading
import me.saket.press.shared.sync.GitHostAuthUiModel.SelectRepo
import me.saket.press.shared.sync.git.GitHost.GITHUB
import me.saket.press.shared.sync.git.service.GitHostService
import me.saket.press.shared.ui.Presenter

@OptIn(ExperimentalListener::class)
class GitHostAuthPresenter constructor(
  httpClient: HttpClient,
  json: Json,
  settings: ObservableSettings,
  private val deepLinks: DeepLinks
) : Presenter<GitHostAuthEvent, GitHostAuthUiModel, GitHostAuthUiEffect>() {

  private val gitHost: GitHost = GITHUB
  private val gitHostService: GitHostService = gitHost.service(httpClient, json)

  private val authToken = Setting.create(
      settings = settings,
      key = "${gitHost.name}_auth_token",
      from = ::GitHostAuthToken,
      to = GitHostAuthToken::value,
      defaultValue = null
  )

  // TODO: get this value from [Syncer] instead.
  private val syncSetupDone = AtomicBoolean(false)

  override fun defaultUiModel() = Loading

  override fun uiModels(): ObservableWrapper<GitHostAuthUiModel> {
    return viewEvents().publish { events ->
      merge(
          completeAuthorization(),
          populateRepositories(),
          selectRepository(events)
      )
    }.wrap()
  }

  override fun uiEffects(): ObservableWrapper<GitHostAuthUiEffect> {
    return requestAuthorization().wrap()
  }

  private fun requestAuthorization(): Observable<OpenAuthorizationUrl> {
    return authToken.listen()
        .doOnBeforeNext { println("requestAuthorization() -> auth token: $it") }
        .filterNull()
        .take(1)
        .map { OpenAuthorizationUrl(gitHostService.generateAuthUrl()) }
  }

  private fun completeAuthorization(): Observable<GitHostAuthUiModel> {
    return deepLinks.listen()
        .filter { it.url.startsWith("intent://press/authorization-granted") }
        .switchMap { link ->
          gitHostService.completeAuth(link.url)
              .asObservable()
              .consumeOnNext<GitHostAuthToken, GitHostAuthUiModel> {
                authToken.set(it)
              }
              .onErrorReturnValue(FullscreenError(onRetry = { deepLinks.broadcast(link) }))
        }
  }

  private fun populateRepositories(): Observable<GitHostAuthUiModel> {
    // todo: handle errors using distinct failure types.
    return authToken.listen()
        .filterNotNull()
        .take(1)
        .switchMapSingle { token ->
          gitHostService.fetchUserRepos(token)
              .map { repos -> SelectRepo(repos) }
              .doOnBeforeSuccess {
                println("Repos: $it")
              }
              .onErrorReturn {
                println("Failed: $it")
                FullscreenError(onRetry = { authToken.set(token) })
              }
        }
  }

  // TODO: handle errors!
  private fun selectRepository(events: Observable<GitHostAuthEvent>): Observable<GitHostAuthUiModel> {
    return events.ofType<GitRepositoryClicked>()
        .switchMapCompletable {
          gitHostService.addDeployKey(
              token = authToken.get()!!,
              repositoryName = it.repo.name,
              sshPublicKey = "foo"
          )
        }
        .andThen(completableFromFunction {
          println("TODO: Close screen")
          syncSetupDone.value = true
          authToken.set(null)
        })
        .asObservable()
  }
}
