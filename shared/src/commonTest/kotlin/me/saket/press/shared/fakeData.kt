package me.saket.press.shared

import com.soywiz.klock.DateTime
import me.saket.press.data.shared.Folder
import me.saket.press.data.shared.FolderQueries
import me.saket.press.data.shared.Note
import me.saket.press.data.shared.NoteQueries
import me.saket.press.shared.db.FolderId
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.syncer.SyncState
import me.saket.press.shared.syncer.SyncState.PENDING
import me.saket.press.shared.syncer.git.GitHost.GITHUB
import me.saket.press.shared.syncer.git.service.GitRepositoryInfo
import me.saket.press.shared.time.Clock
import me.saket.press.shared.time.FakeClock

fun FolderQueries.testInsert(vararg folders: Folder) {
  folders.forEach { this.testInsert(it) }
}

fun NoteQueries.testInsert(vararg notes: Note) {
  notes.forEach { this.testInsert(it) }
}

fun fakeNote(
  content: String,
  id: NoteId = NoteId.generate(),
  folderId: FolderId? = null,
  clock: Clock = FakeClock(),
  createdAt: DateTime = clock.nowUtc(),
  updatedAt: DateTime = createdAt,
  isPendingDeletion: Boolean = false,
  syncState: SyncState = PENDING
): Note {
  return Note(
    id = id,
    folderId = folderId,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isPendingDeletion = isPendingDeletion,
    syncState = syncState
  )
}

fun fakeFolder(
  name: String,
  id: FolderId = FolderId.generate(),
  parent: FolderId? = null
) = Folder(
  id = id,
  name = name,
  parent = parent
)

fun fakeGitRepository(
  name: String = "nationaltreasure"
): GitRepositoryInfo {
  return GitRepositoryInfo(
    host = GITHUB,
    name = name,
    owner = "cage",
    url = "https://github.com/cage/nationaltreasure",
    sshUrl = "git@github.com:cage/nationaltreasure.git",
    defaultBranch = "trunk"
  )
}
