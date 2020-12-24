package me.saket.press.shared.sync.git

import io.ktor.http.Url
import me.saket.press.shared.sync.git.service.GitHostService
import me.saket.press.shared.sync.git.service.GitHostServiceArgs
import me.saket.press.shared.sync.git.service.GitHubService

/**
 * A git host let's the user grant Press access to one of their repositories to sync notes with.
 * Press will ship with GitHub to cover the majority of users, but contributions are welcome to
 * add additional hosts.
 *
 * Adding a new host will require Press to be registered as a developer app. I'll be happy to do
 * this if someone's willing to contribute the code. No UI changes are needed for adding new hosts.
 * Press will display a button in for each host automatically.
 */
enum class GitHost {
  GITHUB {
    override fun displayName() = "GitHub"
    override fun service(args: GitHostServiceArgs) = GitHubService(args)
    override fun newRepoUrl(owner: String, name: String) = "https://github.com/$owner/$name"
  };

  abstract fun displayName(): String
  abstract fun service(args: GitHostServiceArgs): GitHostService
  abstract fun newRepoUrl(owner: String, name: String): String

  fun deepLink() = "intent://press/authorization-granted?githost=$name"

  companion object {
    fun readHostFromDeepLink(link: String): GitHost {
      val serialized = Url(link).parameters["githost"]!!
      return valueOf(serialized)
    }
  }
}
