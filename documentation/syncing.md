# Syncing

Press can sync notes users through a git repository. Instead of locking them into a proprietary system, their notes are theirs to own. The notes are saved as standard markdown (`.md`) files so that they can be accessed on devices where using Press isn't possible or desired. By using git as the underlying sync mechanism, Press is also able to offer users a version history that can be used for recovering accidental edits or deletions by the user as well as Press (because syncing is hard).

GitHub is currently the only available git host, but contributions for adding new hosts are welcome. If you're interested, [add a new enum here](https://github.com/saket/press/blob/trunk/shared/src/commonMain/kotlin/me/saket/press/shared/syncer/git/GitHost.kt) and let the compilation errors guide you.

### Working

- [GitSyncer](https://github.com/saket/press/blob/trunk/shared/src/commonMain/kotlin/me/saket/press/shared/syncer/git/GitSyncer.kt)
- [GitSyncerTest](https://github.com/saket/press/blob/trunk/shared/src/commonTest/kotlin/me/saket/press/shared/syncer/GitSyncerTest.kt)

Press primarily treats git as a tool for syncing files and not for storing them (which is handled using SQLite). On every sync,

1. The git directory is reset to the `last-pushed SHA1` (if present). Any unsynced or dirty changes are discarded. Each sync is treated as a transaction. If Press enters an unexpected state, the [git directory is reset](https://github.com/saket/press/blob/62f2134d22c3c9b6a1bd372e4c8f27cbf729b969/shared/src/commonMain/kotlin/me/saket/press/shared/sync/git/GitSyncer.kt#L175) to the last good state.
2. When syncing for the first time, all local notes are [backed up](https://github.com/saket/press/blob/62f2134d22c3c9b6a1bd372e4c8f27cbf729b969/shared/src/commonMain/kotlin/me/saket/press/shared/sync/git/GitSyncer.kt#L195) in a separate branch. If Press makes any mistakes, this branch can be used for recovering notes.
3. Next, upstream note changes are fetched and the local git branch is rebased onto it. At this step, merge conflicts aren't expected because Press ensures they don't happen. More on this in step 5.
4. Local changes are fetched from the SQLite database and committed. If Press detects that changes were made to files that were also pulled in step 2, they are outright treated as conflicts. When syncing for the first time, the [latest copy is picked](https://github.com/saket/press/blob/62f2134d22c3c9b6a1bd372e4c8f27cbf729b969/shared/src/commonMain/kotlin/me/saket/press/shared/sync/git/GitSyncer.kt#L346) by comparing timestamps. For subsequent syncs, the [local copy is duplicated](https://github.com/saket/press/blob/62f2134d22c3c9b6a1bd372e4c8f27cbf729b969/shared/src/commonMain/kotlin/me/saket/press/shared/sync/git/GitSyncer.kt#L373) as a new note. In case a conflicted note is open on the local device, Press [blocks the user](https://github.com/saket/press/blob/d45633dfbe20f023d2d34c19c4fa757a2da8f6ad/shared/src/commonMain/kotlin/me/saket/press/shared/editor/EditorPresenter.kt#L119) from making any further changes to it. 
5. Committed changes are pushed to remote. If the updates are rejected because remote contains work that Press does not locally, [syncing is aborted](https://github.com/saket/press/blob/62f2134d22c3c9b6a1bd372e4c8f27cbf729b969/shared/src/commonMain/kotlin/me/saket/press/shared/sync/git/GitSyncer.kt#L570) and all new changes are discarded on the next occurrence of step 1. Press ensures that it's always working with the latest copy of user's notes to avoid merge conflicts. 
6. All new pulled and committed [changes are saved](https://github.com/saket/press/blob/62f2134d22c3c9b6a1bd372e4c8f27cbf729b969/shared/src/commonMain/kotlin/me/saket/press/shared/sync/git/GitSyncer.kt#L421) to the SQLite database. 
7. The HEAD is [saved](https://github.com/saket/press/blob/62f2134d22c3c9b6a1bd372e4c8f27cbf729b969/shared/src/commonMain/kotlin/me/saket/press/shared/sync/git/GitSyncer.kt#L594) as the `last-synced SHA1` for use in step 1.

### Representation of notes on the file system

Press tries really hard to avoid leaking Press's implementation into user's git repository. This includes using human readable filenames for saving notes, generated from notes' H1 headings. Considering that multiple notes can have the same heading (e.g., `Shopping list`), Press maintains a canonical directory for mapping unique file names (e.g., `shopping_list.md`, `shopping_list_2.md`) with their SQLite IDs.

See [FileNameRegisterTest](https://github.com/saket/press/blob/trunk/shared/src/commonTest/kotlin/me/saket/press/shared/syncer/git/FileNameRegisterTest.kt) for more info.

### Future improvements

- Press's way of detecting conflicts is very rudimentary. It could use git's [recursive strategy](https://git-scm.com/docs/merge-strategies#Documentation/merge-strategies.txt-recursive) for auto-merging conflicts.

- Press stores a repository's entire git history on all devices. For repositories with a lot of notes, their size can grow large and potentially take a lot of space. It'd be nice to only maintain a shallow copy of repositories.
