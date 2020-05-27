package me.saket.press.shared.sync

import me.saket.kgit.Git
import me.saket.kgit.GitAuthor
import me.saket.kgit.SshConfig
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.InternalStorage

class GitSyncer(git: Git, storage: InternalStorage) : Syncer {
  private val repository = git.repository(storage.path)

  override fun onUpdateContent(note: Note) {
    repository.addAll()
    repository.commit(
        message = "Update note",
        author = GitAuthor("Saket", "saket@somewhere.com")
    )
    repository.push()
  }

  fun setRemote(remoteUrl: String) {
    repository.addRemote("origin", remoteUrl)
  }
}
