package me.saket.press.shared.syncer.git

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.map
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.localization.ENGLISH_STRINGS
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryCreatedResult
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryPresenter
import me.saket.press.shared.rx.RxRule
import me.saket.press.shared.rx.test
import me.saket.press.shared.settings.FakeSetting
import me.saket.press.shared.syncer.git.GitHost.GITHUB
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryEvent.NameTextChanged
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryEvent.SubmitClicked
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryPresenter.Args
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryScreenKey
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryUiModel
import me.saket.press.shared.syncer.git.service.ApiResult
import me.saket.press.shared.syncer.git.service.GitRepositoryInfo
import me.saket.press.shared.syncer.git.service.NewGitRepositoryInfo
import me.saket.press.shared.ui.Back
import me.saket.press.shared.ui.FakeNavigator
import kotlin.test.AfterTest
import kotlin.test.Test

class NewGitRepositoryPresenterTest : BaseDatabaeTest() {
  private val navigator = FakeNavigator()
  private val gitService = FakeGitHostService()
  private val authToken = FakeSetting(GitHostAuthToken("nicolas.cage"))
  private val rxRule = RxRule()

  private fun presenter(
    username: String = "niccage",
    preFilledRepoName: String = ""
  ) = NewGitRepositoryPresenter(
    args = Args(
      screenKey = NewGitRepositoryScreenKey(
        username = username,
        gitHost = GITHUB,
        preFilledRepoName = preFilledRepoName
      ),
      navigator = navigator
    ),
    strings = ENGLISH_STRINGS,
    gitHostService = { gitService },
    authToken = { authToken }
  )

  @AfterTest
  fun finish() {
    rxRule.assertEmpty()
  }

  @Test fun `enable submit button only if repo name is non-empty`() {
    val presenter = presenter()

    val models = presenter
      .models()
      .map { it.submitEnabled }
      .distinctUntilChanged()
      .test(rxRule)

    presenter.dispatch(NameTextChanged(name = ""))
    models.assertValue(false)

    presenter.dispatch(NameTextChanged(name = "ghost"))
    models.assertValue(true)
  }

  @Test fun `show sanitized url preview`() {
    val presenter = presenter(username = "cage").apply {
      dispatch(NameTextChanged(name = "N@me_w1th **sym.bols**"))
    }

    presenter.models()
      .map { it.repoUrlPreview }
      .distinctUntilChanged()
      .test(rxRule)
      .assertValue("https://github.com/cage/N-me_w1th-sym.bols-")
  }

  @Test fun `create repo on submit`() {
    val presenter = presenter(username = "cage")
    val models = presenter.models().test(rxRule)

    presenter.run {
      dispatch(NameTextChanged(name = "national-treasure"))
      dispatch(SubmitClicked)
    }
    models.assertAnyValue()
    models.assertValue(
      NewGitRepositoryUiModel(
        repoUrlPreview = "https://github.com/cage/national-treasure",
        errorMessage = null,
        submitEnabled = false,
        isLoading = true
      )
    )

    assertThat(gitService.authToken.value).isEqualTo(authToken.get())
    assertThat(gitService.newRepoRequest.value).isEqualTo(
      NewGitRepositoryInfo(name = "national-treasure", private = true)
    )
  }

  @Test fun `show failure if repo creation fails`() {
    val presenter = presenter(username = "cage")
    val models = presenter.models().test(rxRule)

    presenter.run {
      dispatch(NameTextChanged(name = "national-treasure"))
      dispatch(SubmitClicked)
    }
    models.popAllValues()

    gitService.newRepoResult.onNext(
      ApiResult.Failure(errorMessage = "name already exists on this account")
    )
    models.assertValue(
      NewGitRepositoryUiModel(
        repoUrlPreview = "https://github.com/cage/national-treasure",
        errorMessage = "name already exists on this account",
        submitEnabled = true,
        isLoading = false
      )
    )
  }

  @Test fun `finish if repo creation succeeds`() {
    val presenter = presenter(username = "cage")
    val models = presenter.models().test(rxRule)

    presenter.run {
      dispatch(NameTextChanged(name = "national-treasure"))
      dispatch(SubmitClicked)
    }
    models.popAllValues()

    val newRepo = GitRepositoryInfo(
      host = GITHUB,
      name = "national-treasure",
      owner = "cage",
      url = "https://youtu.be/dQw4w9WgXcQ",
      sshUrl = "https://youtu.be/dQw4w9WgXcQ",
      defaultBranch = "trunk"
    )
    gitService.newRepoResult.onNext(ApiResult.Success(newRepo))
    models.assertEmpty()

    assertThat(navigator.pop()).isEqualTo(
      Back(result = NewGitRepositoryCreatedResult(newRepo))
    )
  }
}
