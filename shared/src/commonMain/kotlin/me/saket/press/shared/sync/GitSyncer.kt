package me.saket.press.shared.sync

import me.saket.kgit.GitAuthor
import me.saket.kgit.GitRepository
import me.saket.press.data.shared.Note

class GitSyncer(private val git: GitRepository) : Syncer {
  override fun onUpdateContent(note: Note) {
    git.addAll()
    git.commit(
        message = "Update note",
        author = GitAuthor("Saket", "saket@somewhere.com")
    )
    git.push()
  }
}
