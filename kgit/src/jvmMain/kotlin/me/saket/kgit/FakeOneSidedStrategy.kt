package me.saket.kgit

import org.eclipse.jgit.attributes.Attributes
import org.eclipse.jgit.dircache.DirCacheBuildIterator
import org.eclipse.jgit.dircache.DirCacheEntry
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.ObjectInserter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.merge.Merger
import org.eclipse.jgit.merge.ResolveMerger
import org.eclipse.jgit.merge.StrategyResolve
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.WorkingTreeIterator
import org.eclipse.jgit.merge.MergeStrategy as JgitMergeStrategy

/**
 * A "ours" merge strategy disguised as a [StrategyResolve] strategy, because stupid JGit will
 * always type-cast the strategy as a ResolveMerger and fail if something else is used.
 */
class FakeOneSidedStrategy : JgitMergeStrategy() {

  override fun getName(): String = OURS.name
  override fun newMerger(db: Repository): Merger = FakeMerger(db)

  override fun newMerger(db: Repository, inCore: Boolean): Merger = TODO()
  override fun newMerger(inserter: ObjectInserter, config: Config): Merger = TODO()

  class FakeMerger(local: Repository) : ResolveMerger(local, true) {

    override fun processEntry(
      base: CanonicalTreeParser?,
      ours: CanonicalTreeParser?,
      theirs: CanonicalTreeParser?,
      index: DirCacheBuildIterator?,
      work: WorkingTreeIterator?,
      ignoreConflicts: Boolean,
      attributes: Attributes?
    ): Boolean {
      // If the same file entry exists in "ours" and "theirs" trees, always prefer "ours".
      if (ours != null && theirs != null) {
        require(!isIndexDirty()) { "Staging area can't be dirty" }
        enterSubtree = true

        val modeO = tw.getRawMode(T_OURS)
        val modeT = tw.getRawMode(T_THEIRS)
        val modeB = tw.getRawMode(T_BASE)

        if (modeO != 0 || modeT != 0 || modeB != 0) {
          var ourDce: DirCacheEntry? = null
          if (index == null || index.dirCacheEntry == null) {
            // create a fake DCE, but only if ours is valid. ours is kept only
            // in case it is valid, so a null ourDce is ok in all other cases.
            if (nonTree(modeO)) {
              ourDce = DirCacheEntry(tw.rawPath)
              ourDce.setObjectId(tw.getObjectId(T_OURS))
              ourDce.fileMode = tw.getFileMode(T_OURS)
            }
          } else {
            ourDce = index.dirCacheEntry
          }

          // My understanding is that ourDce will never be null in case of conflicts
          // and my break-early instinct wants to throw an error, but I'm afraid of
          // breaking things.
          if (ourDce != null) {
            keep(ourDce)
            return true
          }
        }
      }

      return super.processEntry(base, ours, theirs, index, work, ignoreConflicts, attributes)
    }

    private fun isIndexDirty(): Boolean {
      return reflect<ResolveMerger>().method("isIndexDirty").invoke(this) as Boolean
    }

    private fun keep(e: DirCacheEntry): DirCacheEntry {
      return reflect<ResolveMerger>().method("keep", DirCacheEntry::class.java).invoke(this, e) as DirCacheEntry
    }

    private fun nonTree(mode: Int): Boolean {
      return reflect<ResolveMerger>().method("nonTree", Int::class.java).invoke(null, mode) as Boolean
    }
  }
}
