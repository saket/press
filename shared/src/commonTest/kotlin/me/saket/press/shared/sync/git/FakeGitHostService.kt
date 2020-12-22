package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.single.singleFromFunction
import com.badoo.reaktive.utils.atomic.AtomicReference
import me.saket.kgit.GitIdentity
import me.saket.press.shared.sync.git.service.GitHostService
import me.saket.press.shared.sync.git.service.GitHostService.DeployKey
import me.saket.press.shared.sync.git.service.GitRepositoryInfo
import me.saket.press.shared.sync.git.service.NewGitRepositoryInfo

class FakeGitHostService : GitHostService {
  val completeAuth = AtomicReference<(() -> GitHostAuthToken)?>(null)
  override fun completeAuth(callbackUrl: String) = singleFromFunction { completeAuth.value!!.invoke() }

  val userRepos = AtomicReference<(() -> List<GitRepositoryInfo>)?>(null)
  override fun fetchUserRepos(token: GitHostAuthToken) = singleFromFunction { userRepos.value!!.invoke() }

  override fun generateAuthUrl(redirectUrl: String): String = TODO()

  val user = AtomicReference<(() -> GitIdentity)?>(null)
  override fun fetchUser(token: GitHostAuthToken) = singleFromFunction { user.value!!.invoke() }

  val deployedKey = AtomicReference<DeployKey?>(null)
  val deployKeyResult = AtomicReference<(() -> Unit)?>(null)
  override fun addDeployKey(token: GitHostAuthToken, repository: GitRepositoryInfo, key: DeployKey) =
    completableFromFunction {
      deployedKey.value = key
      deployKeyResult.value!!.invoke()
    }

  override fun createNewRepo(token: GitHostAuthToken, repo: NewGitRepositoryInfo): Completable {
    TODO()
  }
}
