package me.saket.press.shared.sync

import me.saket.kgit.Git
import me.saket.kgit.GitAuthor
import me.saket.kgit.PushResult.Success
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.InternalStorage

class GitSyncer(git: Git, private val storage: InternalStorage) : Syncer {
  private val repository = git.repository(storage.path)

  override fun onUpdateContent(note: Note) {
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
