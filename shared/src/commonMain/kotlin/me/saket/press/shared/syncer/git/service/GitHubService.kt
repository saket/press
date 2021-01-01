package me.saket.press.shared.syncer.git.service

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.coroutinesinterop.completableFromCoroutine
import com.badoo.reaktive.coroutinesinterop.singleFromCoroutine
import com.badoo.reaktive.single.Single
import io.ktor.client.call.receive
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType.Application
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.serialization.Serializable
import me.saket.kgit.GitIdentity
import me.saket.press.shared.BuildKonfig
import me.saket.press.shared.syncer.git.GitHost.GITHUB
import me.saket.press.shared.syncer.git.GitHostAuthToken
import me.saket.press.shared.syncer.git.service.GitHostService.DeployKey

class GitHubService(private val args: GitHostServiceArgs) : GitHostService {
  private val http get() = args.http
  private val json get() = args.json

  override fun generateAuthUrl(redirectUrl: String): String {
    return URLBuilder("https://github.com/login/oauth/authorize").apply {
      parameters.apply {
        append("client_id", BuildKonfig.GITHUB_CLIENT_ID)
        append("scope", "repo")
        append("redirect_uri", redirectUrl)
      }
    }.buildString()
  }

  override fun completeAuth(callbackUrl: String): Single<GitHostAuthToken> {
    return singleFromCoroutine {
      val response = http.post<GetAccessTokenResponse>("https://github.com/login/oauth/access_token") {
        contentType(Application.Json)
        body = GetAccessTokenRequest(
          client_id = BuildKonfig.GITHUB_CLIENT_ID,
          client_secret = BuildKonfig.GITHUB_CLIENT_SECRET,
          code = Url(callbackUrl).parameters["code"]!!
        )
      }
      GitHostAuthToken(response.access_token)
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  override fun fetchUserRepos(token: GitHostAuthToken): Single<List<GitRepositoryInfo>> {
    return singleFromCoroutine {
      var pageNum = 1
      var hasNextPage = true

      return@singleFromCoroutine buildList {
        while (hasNextPage) {
          val response = http.get<HttpResponse>("https://api.github.com/user/repos") {
            accept(Application.Json)
            header("Authorization", "token ${token.value}")
            parameter("per_page", "100")
            parameter("page", "${pageNum++}")
          }

          val repositories = response.receive<List<GitHubRepo>>()
          addAll(
            repositories.map { it.toRepoInfo() }
          )

          hasNextPage = "rel=\"next\"" in response.headers["Link"].orEmpty()
        }
      }
    }
  }

  override fun fetchUser(token: GitHostAuthToken): Single<GitIdentity> {
    return singleFromCoroutine {
      val response = http.get<GithubUserResponse>("https://api.github.com/user") {
        accept(Application.Json)
        header("Authorization", "token ${token.value}")
      }
      GitIdentity(name = response.login, email = response.email)
    }
  }

  override fun addDeployKey(token: GitHostAuthToken, repository: GitRepositoryInfo, key: DeployKey): Completable {
    return completableFromCoroutine {
      http.post<String>("https://api.github.com/repos/${repository.owner}/${repository.name}/keys") {
        header("Authorization", "token ${token.value}")
        contentType(Application.Json)
        body = CreateDeployKeyRequest(
          title = key.title,
          key = key.key.publicKey,
          read_only = false
        )
      }
    }
  }

  override fun createNewRepo(
    token: GitHostAuthToken,
    repo: NewGitRepositoryInfo
  ): Single<ApiResult<GitRepositoryInfo>> {
    check('/' !in repo.name)
    return singleFromCoroutine {
      try {
        val response = http.post<HttpResponse>("https://api.github.com/user/repos") {
          header("Authorization", "token ${token.value}")
          contentType(Application.Json)
          body = CreateRepoRequest(
            name = repo.name,
            private = repo.private,
            has_issues = false,
            has_projects = false,
            has_wiki = false,
          )
        }

        if (response.isSuccessful()) {
          ApiResult.Success(response.receive<GitHubRepo>().toRepoInfo())
        } else {
          ApiResult.Failure(readErrorMessage(response))
        }
      } catch (e: Throwable) {
        ApiResult.Failure(errorMessage = null)
      }
    }
  }

  private suspend fun readErrorMessage(response: HttpResponse): String? {
    return try {
      response.content.readUTF8Line()?.let { errorJson ->
        json.decodeFromString(GithubError.serializer(), errorJson).errorMessage
      }
    } catch (e: Throwable) {
      // Unexpected error json.
      null
    }
  }
}

private fun GitHubRepo.toRepoInfo(): GitRepositoryInfo {
  return GitRepositoryInfo(
    host = GITHUB,
    owner = this.owner.login,
    name = this.name,
    url = this.html_url,
    sshUrl = this.ssh_url,
    defaultBranch = this.default_branch
  )
}

private fun HttpResponse.isSuccessful(): Boolean {
  return status.value in 200..299
}

@Serializable
private data class GithubError(private val errors: List<Error>) {
  val errorMessage: String? get() = errors.firstOrNull()?.message

  @Serializable
  data class Error(
    val message: String
  )
}

@Serializable
private data class GetAccessTokenRequest(
  val client_id: String,
  val client_secret: String,
  val code: String
)

@Serializable
private data class GetAccessTokenResponse(
  val access_token: String
)

@Serializable
private data class GitHubRepo(
  val name: String,
  val owner: Owner,
  val html_url: String,
  val ssh_url: String,
  val default_branch: String
) {
  @Serializable
  data class Owner(val login: String)
}

@Serializable
private data class GithubUserResponse(
  val login: String,
  val email: String?
)

@Serializable
private data class CreateDeployKeyRequest(
  val title: String,
  val key: String,
  val read_only: Boolean
)

@Serializable
private data class CreateRepoRequest(
  val name: String,
  val private: Boolean,
  val has_issues: Boolean,
  val has_projects: Boolean,
  val has_wiki: Boolean
)
