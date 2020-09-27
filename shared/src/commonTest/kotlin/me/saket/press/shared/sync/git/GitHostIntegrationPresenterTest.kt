package me.saket.press.shared.sync.git

import assertk.assertThat
import assertk.assertions.isNullOrEmpty
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.test.base.assertNotError
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.test
import io.ktor.client.HttpClient
import me.saket.press.shared.containsOnly
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.fakedata.fakeRepository
import me.saket.press.shared.settings.FakeSetting
import me.saket.press.shared.sync.git.FailureKind.Authorization
import me.saket.press.shared.sync.git.FailureKind.FetchingRepos
import me.saket.press.shared.sync.git.GitHost.GITHUB
import me.saket.press.shared.sync.git.GitHostIntegrationPresenter.Args
import me.saket.press.shared.sync.git.GitHostIntegrationUiModel.ShowFailure
import me.saket.press.shared.testDeviceInfo
import me.saket.press.shared.ui.FakeNavigator
import kotlin.test.Test

class GitHostIntegrationPresenterTest : BaseDatabaeTest() {
  private val cachedRepos = GitRepositoryCache.InMemory()
  private val gitService = FakeGitHostService()

  private val presenter = GitHostIntegrationPresenter(
      args = Args(deepLink = GITHUB.deepLink(), navigator = FakeNavigator()),
      httpClient = HttpClient(),
      authToken = { FakeSetting(null) },
      gitHostService = { _, _ -> gitService },
      cachedRepos = cachedRepos,
      syncCoordinator = FakeSyncCoordinator(),
      database = database,
      deviceInfo = testDeviceInfo()
  )

  @Test fun `show error if auth fails`() {
    gitService.completeAuth.value = { error("boom!") }

    presenter.uiModels()
        .ofType<ShowFailure>()
        .test()
        .run {
          assertValue(ShowFailure(kind = Authorization))
          assertNotError()
        }
  }

  @Test fun `show error if fetching of repositories fails`() {
    gitService.completeAuth.value = { GitHostAuthToken("nicolas.cage") }
    gitService.userRepos.value = { error("boom!") }

    presenter.uiModels()
        .ofType<ShowFailure>()
        .test()
        .run {
          assertValue(ShowFailure(kind = FetchingRepos))
          assertNotError()
        }
  }

  @Test fun `fetch repositories only if it isn't cached yet`() {
    cachedRepos.set(emptyList())  // cache is non-empty (even if it's an empty list).
    gitService.completeAuth.value = { GitHostAuthToken("nicolas.cage") }
    gitService.userRepos.value = { listOf(fakeRepository().copy(name = "NicCage")) }

    presenter.uiModels().test().assertNotError()
    assertThat(cachedRepos.listen().test().values.last()).isNullOrEmpty()
  }

  @Test fun `skip fetching of repositories if it's already in cache`() {
    cachedRepos.set(null)
    gitService.completeAuth.value = { GitHostAuthToken("nicolas.cage") }
    gitService.userRepos.value = { listOf(fakeRepository().copy(name = "NicCage")) }

    presenter.uiModels().test().assertNotError()
    assertThat(cachedRepos.listen().test().values.last()).containsOnly(fakeRepository().copy(name = "NicCage"))
  }

  @Test fun `show empty view if user doesn't have any repository`() {
    // TODO.
  }
}
