package me.saket.press.shared.sync.git

import okio.buffer
import okio.sink
import okio.source
import java.io.File as JavaFile

actual class PlatformFile constructor(private val delegate: JavaFile) : File {
  actual constructor(parentPath: String, name: String) : this(JavaFile(parentPath, name))
  actual constructor(path: String) : this(JavaFile(path))

  override val path: String get() = delegate.path
  override val name: String get() = delegate.name
  override val exists: Boolean get() = delegate.exists()
  override val parent: File? get() = delegate.parentFile?.let(::PlatformFile)

  override fun write(input: String) {
    delegate.sink().buffer().use {
      it.writeUtf8(input)
    }
  }

  override fun read(): String {
    delegate.source().buffer().use {
      return it.readUtf8()
    }
  }

  override fun copy(name: String, recursively: Boolean): File {
    check(exists)
    check(delegate.parent != null)
    check(recursively) { "todo non-recursive copy" }

    val targetDelegate = JavaFile(delegate.parent, name)
    val copied = delegate.copyRecursively(target = targetDelegate, overwrite = true)
    check(copied) { "Couldn't copy file ($this) to $name" }
    return PlatformFile(targetDelegate)
  }

  override fun renameTo(newName: String): File {
    check(name != newName)
    check(exists) { "$newName doesn't exist" }

    val renamedDelegate = JavaFile(delegate.parent, newName)
    check(!renamedDelegate.exists())

    val renamed = delegate.renameTo(renamedDelegate)
    check(renamed) { "Couldn't rename ($this) to $newName" }
    return PlatformFile(renamedDelegate)
  }

  override fun makeDirectory(recursively: Boolean) {
    if (recursively) {
      delegate.mkdirs()
    } else {
      delegate.mkdir()
    }
  }

  override fun delete(recursively: Boolean) {
    if (recursively) {
      delegate.deleteRecursively()
    } else {
      check(delegate.delete()) { "Failed to delete file: $this" }
    }
  }

  private fun JavaFile.deleteRecursively() {
    if (isDirectory) {
      for (child in listFiles()!!) {
        child.deleteRecursively()
      }
    }
    check(delete()) { "Failed to delete file: $this" }
  }

  override fun children(): List<File> {
    val children = delegate.listFiles() ?: error("Can't print children. Exists? $exists. Path: $path")
    return children.map { PlatformFile(it) }
  }

  override fun toString(): String {
    return "${delegate.name} (${delegate.path})"
  }
}
