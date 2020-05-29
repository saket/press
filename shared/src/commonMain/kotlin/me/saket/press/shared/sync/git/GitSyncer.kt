package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.take
import me.saket.kgit.Git
import me.saket.kgit.GitAuthor
import me.saket.kgit.GitRepository
import me.saket.kgit.PushResult.Success
import me.saket.press.shared.home.SplitHeadingAndBody
import me.saket.press.shared.note.NoteRepository
import me.saket.press.shared.sync.Syncer

class GitSyncer(
  git: Git,
  appStorage: AppStorage,
  private val noteRepository: NoteRepository
) : Syncer {

  private val directory = File(appStorage.path, "git").apply { makeDirectory() }
  private val git: GitRepository = git.repository(directory.path)

  override fun sync(): Completable {
    return commitAllChanges()
        .andThen(pull())
        .andThen(push())
  }

  // TODO: commit deleted and archived notes as well.
  private fun commitAllChanges(): Completable {
    return noteRepository.notes().take(1).flatMapCompletable { notes ->
      completableFromFunction {
        for (note in notes) {
          val (heading) = SplitHeadingAndBody.split(note.content)
          check(heading.isNotBlank()) { "Heading is empty for: '${note.content}'" }

          val noteFileName = FileNameSanitizer.sanitize(heading, maxLength = 255)
          File(directory.path, "$noteFileName.md").write(note.content)
        }

        git.addAll()
        git.commit(
            message = "Update notes",
            author = GitAuthor("Saket", "pressapp@saket.me")
        )
      }
    }
  }

  private fun pull(): Completable {
    return completableFromFunction {
      git.pull(rebase = true)
    }
  }

  private fun push(): Completable {
    return completableFromFunction {
      val pushResult = git.push()
      require(pushResult is Success) { "Failed to push: $pushResult" }
    }
  }

  fun setRemote(remoteUrl: String) {
    git.addRemote("origin", remoteUrl)
  }
}
