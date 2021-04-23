package me.saket.press.shared.syncer.git

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import co.touchlab.stately.concurrency.value
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.filter
import me.saket.kgit.GitIdentity
import me.saket.kgit.SshKeyPair
import me.saket.kgit.SshPrivateKey
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.fakeRepository
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationPresenter
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryScreenKey
import me.saket.press.shared.rx.RxRule
import me.saket.press.shared.rx.test
import me.saket.press.shared.settings.FakeSetting
import me.saket.press.shared.preferences.sync.setup.FailureKind.AddingDeployKey
import me.saket.press.shared.preferences.sync.setup.FailureKind.Authorization
import me.saket.press.shared.preferences.sync.setup.FailureKind.FetchingRepos
import me.saket.press.shared.syncer.git.GitHost.GITHUB
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationEvent.CreateNewGitRepoClicked
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationEvent.GitRepositoryClicked
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationEvent.RetryClicked
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationEvent.SearchTextChanged
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationPresenter.Args
import me.saket.press.shared.preferences.sync.setup.GitRepositoryCache
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationUiModel.SelectRepo
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationUiModel.ShowFailure
import me.saket.press.shared.preferences.sync.setup.GitHostIntegrationUiModel.ShowProgress
import me.saket.press.shared.syncer.git.service.GitHostService.DeployKey
import me.saket.press.shared.testDeviceInfo
import me.saket.press.shared.ui.Back
import me.saket.press.shared.ui.FakeNavigator
import me.saket.press.shared.ui.ScreenResults
import kotlin.test.AfterTest
import kotlin.test.Test

class GitHostIntegrationPresenterTest : BaseDatabaeTest() {
  private val cachedRepos = GitRepositoryCache.InMemory()
  private val gitService = FakeGitHostService()
  private val authToken = FakeSetting<GitHostAuthToken>(null)
  private val userSetting = FakeSetting<GitIdentity>(null)
  private val navigator = FakeNavigator()
  private val syncCoordinator = FakeSyncCoordinator()
  private val sshKeygen = FakeSshKeygen()
  private val rxRule = RxRule()

  private val presenter = GitHostIntegrationPresenter(
    args = Args(deepLink = GITHUB.deepLink(), navigator),
    gitHostService = { gitService },
    authToken = { authToken },
    userSetting = userSetting,
    cachedRepos = cachedRepos,
    syncCoordinator = syncCoordinator,
    database = database,
    deviceInfo = testDeviceInfo(),
    sshKeygen = sshKeygen,
    screenResults = ScreenResults()
  )

  @AfterTest
  fun finish() {
    rxRule.assertEmpty()
  }

  @Test fun `retry auth`() {
    gitService.completeAuth.value = { error("boom!") }

    val models = presenter.models()
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
    gitService.userRepos.value = { error("repo fetch failure") }
    gitService.user.value = { error("user fetch failure") }
    presenter.dispatch(SearchTextChanged("")) // Initial event sent by the view.

    val models = presenter.models()
      .distinctUntilChanged()
      .test(rxRule)
      .assertValue(ShowProgress)
      .assertValue(ShowFailure(kind = FetchingRepos))

    gitService.userRepos.value = { listOf(fakeRepository()) }
    presenter.dispatch(RetryClicked(failure = FetchingRepos))

    models
      .assertValue(ShowProgress)
      .assertValue(ShowFailure(kind = FetchingRepos))

    gitService.user.value = { user }

    presenter.dispatch(RetryClicked(failure = FetchingRepos))

    models.assertValue(ShowProgress)
    assertThat(models.popValue()).isInstanceOf(SelectRepo::class)
  }

  @Test fun `retry selection of repo`() {
    cachedRepos.set(emptyList())
    authToken.set(GitHostAuthToken("nicolas.cage"))
    userSetting.set(user)
    sshKeygen.key.value = sshKeyPair

    gitService.deployKeyResult.value = { error("key boom!") }
    presenter.dispatch(GitRepositoryClicked(repo))

    val models = presenter.models()
      .test(rxRule)
      .assertValue(ShowProgress)
      .assertValue(ShowFailure(kind = AddingDeployKey))

    gitService.deployKeyResult.value = { Unit }
    presenter.dispatch(RetryClicked(failure = AddingDeployKey))
    models.assertAnyValue()

    assertThat(gitService.deployedKey.value).isEqualTo(
      DeployKey(
        title = "Press (${testDeviceInfo().deviceName()})",
        key = sshKeyPair
      )
    )

    assertThat(authToken.get()).isNull()
    assertThat(syncCoordinator.triggered.value).isTrue()
    assertThat(navigator.pop()).isEqualTo(Back())

    val syncConfig = database.folderSyncConfigQueries.select().executeAsOne()
    assertThat(syncConfig.remote.remote).isEqualTo(repo)
    assertThat(syncConfig.remote.user).isEqualTo(user)
  }

  @Test fun `skip fetching of repositories if it's already in cache`() {
    cachedRepos.set(emptyList())  // cache is non-empty (even if it's an empty list).
    gitService.completeAuth.value = { GitHostAuthToken("nicolas.cage") }
    gitService.userRepos.value = { listOf(repo) }

    presenter.models()
      .test(rxRule)
      .assertEmpty()

    cachedRepos.listen()
      .test(rxRule)
      .assertValue(emptyList())
  }

  @Test fun `fetch repositories only if it isn't cached yet`() {
    cachedRepos.set(null)
    gitService.completeAuth.value = { GitHostAuthToken("nicolas.cage") }
    gitService.userRepos.value = { listOf(repo) }
    gitService.user.value = { user }

    presenter.models()
      .test(rxRule)
      .assertValue(ShowProgress)
      .popAllValues()

    cachedRepos.listen()
      .test(rxRule)
      .assertValue(listOf(repo))
  }

  @Test fun `show new git repo screen`() {
    userSetting.set(user)

    val models = presenter.models().test(rxRule)

    presenter.run {
      dispatch(SearchTextChanged("cage"))
      dispatch(CreateNewGitRepoClicked)
    }
    models.popAllValues()

    assertThat(navigator.pop()).isEqualTo(
      NewGitRepositoryScreenKey(
        username = user.name,
        gitHost = GITHUB,
        preFilledRepoName = "cage"
      )
    )
  }

  // TODO.
  @Test fun `show empty view if user doesn't have any repository`() {
  }

  @Test fun `delete auth token when a deploy key is added`() {
    cachedRepos.set(emptyList())
    userSetting.set(user)
    gitService.deployKeyResult.value = { }

    authToken.set(GitHostAuthToken("Nicolas Cage's super secret token"))

    presenter.models()
      .filter { false }
      .test(rxRule)
    presenter.dispatch(GitRepositoryClicked(fakeRepository()))

    assertThat(authToken.get()).isNull()
  }

  companion object {
    val repo = fakeRepository(name = "NicCage")
    val user = GitIdentity("nicolas", "nicolas@cage.com")
    val sshKeyPair = SshKeyPair(publicKey = "nicolas cage", privateKey = SshPrivateKey("is a national treasure"))
  }
}
