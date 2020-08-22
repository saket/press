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
  override val isDirectory: Boolean get() = delegate.isDirectory

  override fun write(input: String) {
    parent?.let { check(it.exists) }
    delegate.sink().buffer().use {
      it.writeUtf8(input)
    }
  }

  override fun read(): String {
    check(exists)
    delegate.source().buffer().use {
      return it.readUtf8()
    }
  }

  override fun copy(name: String): File {
    check(exists)
    check(delegate.parent != null)
    if ('/' in name) throw error("todo: copy to another folder")

    val targetDelegate = JavaFile(delegate.parent, name)
    val copied = delegate.copyRecursively(target = targetDelegate, overwrite = true)
    check(copied) { "Couldn't copy file ($this) to $name" }
    return PlatformFile(targetDelegate)
  }

  override fun renameTo(newFile: File): File {
    check(this.path != newFile.path) { "Same path: $path vs ${newFile.path}" }
    check(this.exists) { "$path doesn't exist" }

    check(!newFile.exists)
    if (!newFile.parent!!.exists) {
      newFile.parent!!.makeDirectory(recursively = true)
    }

    val renamed = delegate.renameTo(JavaFile(newFile.path))
    check(renamed) { "Couldn't rename ($this) to $newFile" }
    return newFile
  }

  override fun makeDirectory(recursively: Boolean) {
    if (recursively) {
      delegate.mkdirs()
    } else {
      delegate.mkdir()
    }
  }

  override fun delete() {
    check(exists) { "$name does not exist: $path" }
    check(delegate.delete()) { "Failed to delete file: $this" }
  }

  override fun children(): List<File> {
    check(exists)
    check(delegate.isDirectory)
    val children = delegate.listFiles() ?: error("Can't print children. Exists? $exists. Path: $path")
    return children.map(::PlatformFile)
  }

  override fun toString(): String {
    return "${delegate.name} (${delegate.path})"
  }
}
