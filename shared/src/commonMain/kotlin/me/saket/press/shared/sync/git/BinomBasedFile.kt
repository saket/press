package me.saket.press.shared.sync.git

import pw.binom.ByteBuffer
import pw.binom.charset.Charsets
import pw.binom.io.file.isExist
import pw.binom.io.file.mkdirs
import pw.binom.io.file.name
import pw.binom.io.file.parent
import pw.binom.io.file.read
import pw.binom.io.file.write
import pw.binom.io.readText
import pw.binom.io.reader
import pw.binom.io.use
import pw.binom.toByteBufferUTF8
import pw.binom.io.file.File as BinomFile

/**
 * Really hoping that this can use Okio in the future because everything else is quite... bad.
 * [https://publicobject.com/2020/10/06/files/]
 */
internal class BinomBasedFile(val delegate: BinomFile) : File {
  constructor(path: String) : this(BinomFile(path))

  constructor(parent: File, name: String) : this(
    BinomFile(
      parent = BinomFile(parent.path),
      name = name
    )
  )

  override val exists: Boolean get() = delegate.isExist
  override val path: String get() = delegate.path
  override val name: String get() = delegate.name
  override val parent: File? get() = delegate.parent?.let(::BinomBasedFile)
  override val isDirectory: Boolean get() = delegate.isDirectory

  override fun write(input: String) {
    parent?.let { check(it.exists) }

    val data: ByteBuffer = input.toByteBufferUTF8()
    delegate.write().use {
      it.write(data)
      it.flush()
    }
    data.clear()
  }

  override fun read(): String {
    check(exists) { "Can't read non-existent: $path" }

    return delegate.read().reader(Charsets.UTF8).use {
      it.readText()
    }
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

  override fun sizeInBytes(): Long {
    check(exists)
    check(!isDirectory)
    return delegate.size
  }

  override fun children(): List<File> {
    check(exists)
    check(delegate.isDirectory)

    val children = delegate.list()
    return children.map(::BinomBasedFile)
  }

  /** @return the same [newFile] for convenience. */
  override fun renameTo(newFile: File): File {
    check(this.path != newFile.path) { "Same path: $path vs ${newFile.path}" }
    check(this.exists) { "$path doesn't exist" }
    check(!newFile.exists) { "${newFile.path} already exists!" }

    if (!newFile.parent!!.exists) {
      newFile.parent!!.makeDirectory(recursively = true)
    }

    val renamed = delegate.renameTo(BinomFile(newFile.path))
    check(renamed) { "Couldn't rename ($this) to $newFile" }
    return newFile
  }

  override fun equalsContent(content: String): Boolean {
    delegate.read().reader(Charsets.UTF8).use {
      var offset = 0
      while (true) {
        if (it.read() ?: break != content[offset++]) {
          return false
        }
      }
      return offset == content.length
    }
  }

  override fun toString(): String {
    return "${delegate.name} (${delegate.path})"
  }
}
