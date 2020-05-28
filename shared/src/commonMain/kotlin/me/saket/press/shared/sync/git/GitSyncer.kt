package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.Completable
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
import me.saket.press.shared.util.Locale
import me.saket.press.shared.util.toLowerCase
import me.saket.wysiwyg.util.isDigit

class GitSyncer(
  git: Git,
  appStorage: AppStorage,
  private val noteRepository: NoteRepository
) : Syncer {

  private val directory = File(appStorage.path, "git").apply { makeDirectory() }
  private val repository: GitRepository = git.repository(directory.path)

  override fun sync(): Completable {
    // todo: sync deleted and archived notes as well.
    return noteRepository.notes().take(1).flatMapCompletable { notes ->
      completableFromFunction {
        val note = notes.single()

        val (heading) = SplitHeadingAndBody.split(note.content)
        check(heading.isNotBlank()) { "Heading is empty for: '${note.content}'" }
        
        val noteFileName = FileNameSanitizer.sanitize(heading, maxLength = 255)
        File(directory.path, "$noteFileName.md").write(note.content)

        repository.addAll()
        repository.commit(
            message = "Update note",
            author = GitAuthor("Saket", "saket@somewhere.com")
        )
        val pushResult = repository.push()
        require(pushResult is Success) { "Failed to push: $pushResult" }
      }
    }
  }

  fun setRemote(remoteUrl: String) {
    repository.addRemote("origin", remoteUrl)
  }
}
