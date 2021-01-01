package me.saket.press.shared.syncer.git

import com.soywiz.korio.file.File_separatorChar
import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.baseName
import com.soywiz.korio.file.std.rootLocalVfs
import me.saket.press.shared.runBlocking

// https://github.com/korlibs/korio
internal class KorioBasedFile(path: String) : File {
  private val fs: VfsFile = rootLocalVfs
  private val file = fs[path]

  override val exists: Boolean get() = runBlocking { file.exists() }
  override val path: String get() = file.absolutePath
  override val name: String get() = file.baseName
  override val parent: File get() = file.parent.path.let(::KorioBasedFile)
  override val isDirectory: Boolean get() = runBlocking { file.isDirectory() }

  constructor(parent: File, name: String) : this(parent.path + File_separatorChar + name)

  override fun write(input: String) {
    check(parent.exists) { "parent doesn't exist: $parent" }

    runBlocking {
      file.writeString(input)
    }
  }

  override fun read(): String {
    check(exists) { "Can't read non-existent: $path" }

    return runBlocking {
      file.readString()
    }
  }

  override fun makeDirectories(): File {
    runBlocking {
      // https://github.com/korlibs/korio/issues/109#issuecomment-718209835
      val pending = mutableListOf<VfsFile>()
      var nextPending = file
      while (!nextPending.exists()) {
        pending.add(nextPending)
        nextPending = nextPending.parent
      }
      for (ancestor in pending.reversed()) {
        check(ancestor.mkdir()) { "couldn't create directories for $ancestor" }
      }
    }
    return this
  }

  override fun delete() {
    runBlocking {
      check(exists) { "$name does not exist: $path" }
      val deleted = if (isDirectory) {
        file.vfs.rmdir(path)
      } else {
        file.delete()
      }
      check(deleted) { "Failed to delete file: $file" }
    }
  }

  override fun sizeInBytes(): Long {
    return runBlocking {
      check(file.exists())
      check(!file.isDirectory())

      file.size()
    }
  }

  override fun children(): List<File> {
    return runBlocking {
      check(file.exists())
      check(file.isDirectory())

      file.listSimple()
        // On POSIX, this also includes '.' and '..' entries
        // which point to the current and the parent directory.
        // File can't be empty anyway so this should be safe.
        .filter { it.baseName.isNotBlank() }
        .map {
          // https://github.com/korlibs/korio/issues/111
          val absPath = "${this@KorioBasedFile.path}/${it.baseName}"
          KorioBasedFile(absPath)
        }
    }
  }

  override fun renameTo(newFile: File): File {
    check(this.path != newFile.path) { "Same path: $path vs ${newFile.path}" }
    check(this.exists) { "$path doesn't exist" }
    check(!newFile.exists) { "${newFile.path} already exists!" }

    if (!newFile.parent.exists) {
      newFile.parent.makeDirectories()
    }

    return runBlocking {
      val renamed = file.renameTo(newFile.path)
      check(renamed) { "Couldn't rename ($this) to ${newFile.path}" }
      KorioBasedFile(newFile.path)
    }
  }

  override fun equalsContent(content: String): Boolean {
    return runBlocking {
      file.readString() == content
    }
  }

  override fun toString(): String {
    return "$name ($path)"
  }
}
