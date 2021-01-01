package me.saket.press.shared.preferences.sync.setup

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.combineLatest
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.startWithValue
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.withLatestFrom
import com.badoo.reaktive.observable.wrap
import com.badoo.reaktive.single.asObservable
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.rx.mergeWith
import me.saket.press.shared.preferences.Setting
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryEvent.NameTextChanged
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryEvent.SubmitClicked
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryPresenter.SubmitResult.Failure
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryPresenter.SubmitResult.Idle
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryPresenter.SubmitResult.Ongoing
import me.saket.press.shared.syncer.git.GitHost
import me.saket.press.shared.syncer.git.GitHostAuthToken
import me.saket.press.shared.syncer.git.service.GitHostService
import me.saket.press.shared.syncer.git.service.NewGitRepositoryInfo
import me.saket.press.shared.syncer.git.service.filterFailure
import me.saket.press.shared.syncer.git.service.filterSuccess
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.util.isLetterOrDigit
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryEvent as Event
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryUiModel as Model

class NewGitRepositoryPresenter(
  private val args: Args,
  private val strings: Strings,
  gitHostService: GitHostService.Factory,
  authToken: (GitHost) -> Setting<GitHostAuthToken>,
) : Presenter<Event, Model>() {

  private val screenKey: NewGitRepositoryScreenKey get() = args.screenKey
  private val gitHost: GitHost get() = screenKey.gitHost
  private val gitHostService: GitHostService = gitHostService.create(gitHost)
  private val authToken: Setting<GitHostAuthToken> = authToken(gitHost)

  override fun defaultUiModel() = Model(
    repoUrlPreview = "",
    errorMessage = null,
    isLoading = false,
    submitEnabled = false
  )

  override fun models(): ObservableWrapper<Model> {
    return viewEvents().publish { events ->
      val repoUrls = events
        .ofType<NameTextChanged>()
        .map {
          if (it.name.isBlank()) null
          else gitHost.newRepoUrl(screenKey.username, sanitize(it.name))
        }

      combineLatest(repoUrls, handleSubmits(events)) { repoUrl, submitResult ->
        Model(
          repoUrlPreview = repoUrl,
          isLoading = submitResult is Ongoing,
          errorMessage = if (submitResult is Failure) submitResult.message else null,
          submitEnabled = !repoUrl.isNullOrBlank() && submitResult !is Ongoing
        )
      }
    }.wrap()
  }

  // https://stackoverflow.com/a/59082561/2511884
  private fun sanitize(string: String): String {
    return buildString {
      for (char in string) {
        if (char.isLetterOrDigit() || char == '.' || char == '_') {
          append(char)
        } else if (lastOrNull() != '-') {
          append('-')
        }
      }
    }
  }

  private fun handleSubmits(events: Observable<Event>): Observable<SubmitResult> {
    val repoNames = events
      .ofType<NameTextChanged>()
      .map { sanitize(it.name) }

    return events.ofType<SubmitClicked>()
      .withLatestFrom(repoNames, ::Pair)
      .filter { (_, repo) -> repo.isNotBlank() }
      .switchMap { (_, repoName) ->
        val newRepo = NewGitRepositoryInfo(repoName, private = true)
        gitHostService.createNewRepo(authToken.get()!!, newRepo)
          .asObservable()
          .publish {
            merge(
              it.filterFailure().map {
                Failure(it.errorMessage ?: strings.common.generic_error)
              },
              it.filterSuccess().consumeOnNext { (repo) ->
                args.navigator.goBack(result = NewGitRepositoryCreatedResult(repo))
              }
            )
          }
          .startWithValue(Ongoing)
      }
      .mergeWith(repoNames.map { Idle })
  }

  sealed class SubmitResult {
    object Idle : SubmitResult()
    object Ongoing : SubmitResult()
    data class Failure(val message: String) : SubmitResult()
  }

  data class Args(
    val screenKey: NewGitRepositoryScreenKey,
    val navigator: Navigator
  )

  fun interface Factory {
    fun create(args: Args): NewGitRepositoryPresenter
  }
}
