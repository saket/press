package me.saket.press.shared.sync.git

import me.saket.kgit.Git
import me.saket.kgit.GitAuthor
import me.saket.kgit.GitRepository
import me.saket.kgit.PushResult.Success
import me.saket.press.data.shared.Note
import me.saket.press.shared.home.SplitHeadingAndBody
import me.saket.press.shared.sync.Syncer
import me.saket.press.shared.util.Locale
import me.saket.press.shared.util.toLowerCase

class GitSyncer(git: Git, appStorage: InternalStorage) : Syncer {

  private val directory = File(appStorage.path, "git").apply { makeDirectory() }
  private val repository: GitRepository = git.repository(directory.path)

  override fun onUpdateContent(note: Note) {
    val (heading) = SplitHeadingAndBody.split(note.content)
    check(heading.isNotBlank()) { "Heading is empty for: '${note.content}'" }

    val noteFileName = heading
        .toLowerCase(Locale.US)
        .replace(" ", "_")
        .plus(".md")
    File(directory.path, noteFileName).write(note.content)

    repository.addAll()
    repository.commit(
        message = "Update note",
        author = GitAuthor("Saket", "saket@somewhere.com")
    )
    val pushResult = repository.push()
    require(pushResult is Success) { "Failed to push: $pushResult" }
  }

  fun setRemote(remoteUrl: String) {
    repository.addRemote("origin", remoteUrl)
  }
}
