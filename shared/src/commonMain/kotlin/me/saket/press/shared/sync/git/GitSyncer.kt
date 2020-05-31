package me.saket.press.shared.sync.git

import co.touchlab.stately.concurrency.AtomicReference
import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.take
import me.saket.kgit.GitAuthor
import me.saket.kgit.GitRepository
import me.saket.kgit.PullResult
import me.saket.kgit.PushResult
import me.saket.kgit.UtcTimestamp
import me.saket.press.shared.home.SplitHeadingAndBody
import me.saket.press.shared.note.NoteRepository
import me.saket.press.shared.sync.Syncer

// TODO: commit deleted and archived notes as well.
// TODO: commit only un-synced notes.
// TODO: figure out the author email.
class GitSyncer(
  private val git: GitRepository,
  private val noteRepository: NoteRepository
) : Syncer {

  private val directory = File(git.directoryPath)
  private val remoteSet = AtomicReference(false)

  override fun sync(): Completable {
    require(remoteSet.get()) { "Remote isn't set" }
    return commitAllChanges()
        .andThen(pull())
        .andThen(push())
  }

  private fun commitAllChanges(): Completable {
    val unSyncedNotes = noteRepository.notes()
        .take(1)
        .filter { it.isNotEmpty() }

    return unSyncedNotes.flatMapCompletable { notes ->
      directory.makeDirectory()
      completableFromFunction {
        for (note in notes) {
          val (heading) = SplitHeadingAndBody.split(note.content)
          check(heading.isNotBlank()) { "Heading is empty for: '${note.content}'" }
          val noteFileName = FileNameSanitizer.sanitize(heading, maxLength = 255)
          File(directory, "$noteFileName.md").write(note.content)

          git.addAll()
          git.commit(
              message = "Update '$heading'",
              author = GitAuthor("Saket", "pressapp@saket.me"),
              timestamp = UtcTimestamp(note.updatedAt.unixMillisLong)
          )
        }
      }
    }
  }

  private fun pull(): Completable {
    return completableFromFunction {
      val headBeforePull = git.resolve("HEAD")

      val pullResult = git.pull(rebase = true)
      require(pullResult !is PullResult.Failure) { "Failed to pull: $pullResult" }

      val headAfterPull = git.resolve("HEAD")

      println("headBeforePull: $headBeforePull")
      println("headAfterPull: $headAfterPull\n")
      if (headAfterPull != null) {
        git.diff(headBeforePull, headAfterPull)
      }
    }
  }

  private fun push(): Completable {
    return completableFromFunction {
      val pushResult = git.push()
      require(pushResult !is PushResult.Failure) { "Failed to push: $pushResult" }
    }
  }

  fun setRemote(remoteUrl: String) {
    git.addRemote("origin", remoteUrl)
    remoteSet.set(true)
  }
}
