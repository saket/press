package me.saket.press.shared.sync.git

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import co.touchlab.stately.concurrency.value
import com.badoo.reaktive.observable.distinctUntilChanged
import io.ktor.client.HttpClient
import me.saket.kgit.GitIdentity
import me.saket.kgit.SshKeyPair
import me.saket.kgit.SshPrivateKey
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.fakedata.fakeRepository
import me.saket.press.shared.rx.RxRule
import me.saket.press.shared.rx.test
import me.saket.press.shared.settings.FakeSetting
import me.saket.press.shared.sync.git.FailureKind.AddingDeployKey
import me.saket.press.shared.sync.git.FailureKind.Authorization
import me.saket.press.shared.sync.git.FailureKind.FetchingRepos
import me.saket.press.shared.sync.git.GitHost.GITHUB
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.GitRepositoryClicked
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.RetryClicked
import me.saket.press.shared.sync.git.GitHostIntegrationEvent.SearchTextChanged
import me.saket.press.shared.sync.git.GitHostIntegrationPresenter.Args
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.SelectRepo
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowFailure
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowProgress
import me.saket.press.shared.sync.git.service.GitHostService.DeployKey
import me.saket.press.shared.testDeviceInfo
import me.saket.press.shared.ui.FakeNavigator
import me.saket.press.shared.ui.ScreenKey.Close
import kotlin.test.AfterTest
import kotlin.test.Test

class GitHostIntegrationPresenterTest : BaseDatabaeTest() {
  private val cachedRepos = GitRepositoryCache.InMemory()
  private val gitService = FakeGitHostService()
  private val authToken = FakeSetting<GitHostAuthToken>(null)
  private val navigator = FakeNavigator()
  private val syncCoordinator = FakeSyncCoordinator()
  private val sshKeygen = FakeSshKeygen()
  private val rxRule = RxRule()

  private val presenter = GitHostIntegrationPresenter(
    args = Args(deepLink = GITHUB.deepLink(), navigator),
    httpClient = HttpClient(),
    authToken = { authToken },
    gitHostService = { _, _ -> gitService },
    cachedRepos = cachedRepos,
    syncCoordinator = syncCoordinator,
    database = database,
    deviceInfo = testDeviceInfo(),
    sshKeygen = sshKeygen
  )

  @AfterTest
  fun finish() {
    rxRule.assertEmpty()
  }

  @Test fun `retry auth`() {
    gitService.completeAuth.value = { error("boom!") }

    val models = presenter.uiModels()
      .test(rxRule)
      .assertValue(ShowProgress)
      .assertValue(ShowFailure(kind = Authorization))

    gitService.completeAuth.value = { GitHostAuthToken("nicolas.cage") }
    presenter.dispatch(RetryClicked(failure = Authorization))

    assertThat(authToken.get()).isEqualTo(GitHostAuthToken("nicolas.cage"))

    models
      .assertValue(ShowProgress)
      .popAllValues()
  }

  @Test fun `retry fetching of repositories`() {
    gitService.completeAuth.value = { GitHostAuthToken("nicolas.cage") }
    gitService.userRepos.value = { error("boom!") }
    presenter.dispatch(SearchTextChanged("")) // Initial event sent by the view.

    val models = presenter.uiModels()
      .distinctUntilChanged()
      .test(rxRule)
      .assertValue(ShowProgress)
      .assertValue(ShowFailure(kind = FetchingRepos))

    gitService.userRepos.value = { listOf(fakeRepository()) }
    presenter.dispatch(RetryClicked(failure = FetchingRepos))

    models
      .assertValue(ShowProgress)
      .assertValue { it is SelectRepo }
  }

  @Test fun `retry selection of repo`() {
    cachedRepos.set(emptyList())
    authToken.set(GitHostAuthToken("nicolas.cage"))
    sshKeygen.key.value = sshKeyPair

    gitService.user.value = { error("user boom!") }
    gitService.deployKeyResult.value = { error("key boom!") }
    presenter.dispatch(GitRepositoryClicked(repo))

    val models = presenter.uiModels()
      .test(rxRule)
      .assertValue(ShowProgress)
      .assertValue(ShowFailure(kind = AddingDeployKey))

    gitService.user.value = { user }
    presenter.dispatch(RetryClicked(failure = AddingDeployKey))
    models
      .assertValue(ShowProgress)
      .assertValue(ShowFailure(kind = AddingDeployKey))

    gitService.deployKeyResult.value = { Unit }
    presenter.dispatch(RetryClicked(failure = AddingDeployKey))
    models.popAllValues()

    assertThat(gitService.deployedKey.value).isEqualTo(
      DeployKey(
        title = "Press (${testDeviceInfo().deviceName()})",
        key = sshKeyPair
      )
    )

    assertThat(authToken.get()).isNull()
    assertThat(syncCoordinator.triggered.value).isTrue()
    assertThat(navigator.pop()).isEqualTo(Close)

    val syncConfig = database.folderSyncConfigQueries.select().executeAsOne()
    assertThat(syncConfig.remote.remote).isEqualTo(repo)
    assertThat(syncConfig.remote.user).isEqualTo(user)
  }

  @Test fun `fetch repositories only if it isn't cached yet`() {
    cachedRepos.set(emptyList())  // cache is non-empty (even if it's an empty list).
    gitService.completeAuth.value = { GitHostAuthToken("nicolas.cage") }
    gitService.userRepos.value = { listOf(repo) }

    presenter.uiModels()
      .test(rxRule)
      .assertEmpty()

    cachedRepos.listen()
      .test(rxRule)
      .assertValue(emptyList())
  }

  @Test fun `skip fetching of repositories if it's already in cache`() {
    cachedRepos.set(null)
    gitService.completeAuth.value = { GitHostAuthToken("nicolas.cage") }
    gitService.userRepos.value = { listOf(repo) }

    presenter.uiModels()
      .test(rxRule)
      .assertValue(ShowProgress)
      .popAllValues()

    cachedRepos.listen()
      .test(rxRule)
      .assertValue(listOf(repo))
  }

  @Test fun `show empty view if user doesn't have any repository`() {
    // TODO.
  }

  companion object {
    val repo = fakeRepository(name = "NicCage")
    val user = GitIdentity("nicolas", "nicolas@cage.com")
    val sshKeyPair = SshKeyPair(publicKey = "nicolas cage", privateKey = SshPrivateKey("is a national treasure"))
  }
}
