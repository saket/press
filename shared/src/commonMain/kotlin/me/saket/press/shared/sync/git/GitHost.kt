package me.saket.press.shared.sync.git

import io.ktor.client.HttpClient

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
    override fun service(http: HttpClient) = GitHubService(http)
  };

  abstract fun service(http: HttpClient): GitHostService
}
