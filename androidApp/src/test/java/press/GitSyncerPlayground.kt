package press

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.soywiz.klock.DateTime
import me.saket.kgit.Git
import me.saket.press.BuildConfig
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.DateTimeAdapter
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.sync.GitSyncer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import timber.log.Timber
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])  // API 29 requires Java 9.
class GitSyncerUnitPlayground {

  private val context: Context
    get() = ApplicationProvider.getApplicationContext()

  init {
    Timber.plant(object : Timber.DebugTree() {
      override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        println("$tag: $message")
      }
    })
  }

  @Test
  fun foo() {
    val git = Git.repository(context.filesDir.path)
    git.addRemote("origin", "git@github.com:saket/PressSyncPlayground.git")

    val syncer = GitSyncer(git = git)
    syncer.onUpdateContent(
        note = fakeNote(content = "Nicolas Cage is a national treasure"),
        sshPrivateKey = BuildConfig.GITHUB_SSH_PRIV_KEY
    )
  }
}

private fun fakeNote(
  localId: Long = Random.Default.nextLong(),
  noteId: NoteId = NoteId.generate(),
  content: String,
  createdAt: DateTime = DateTime.now(),
  updatedAt: DateTime = DateTime.now(),
  archivedAt: DateTime? = null,
  deletedAt: DateTime? = null
): Note.Impl {
  return Note.Impl(
      localId = localId,
      uuid = noteId,
      content = content,
      createdAt = createdAt,
      updatedAt = updatedAt,
      archivedAtString = archivedAt?.let { DateTimeAdapter.encode(it) },
      deletedAtString = deletedAt?.let { DateTimeAdapter.encode(it) }
  )
}
