package me.saket.press.shared.sync.git

import pw.binom.ByteBuffer
import pw.binom.ByteBufferPool
import pw.binom.asUTF8String
import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.file.isExist
import pw.binom.io.file.mkdirs
import pw.binom.io.file.name
import pw.binom.io.file.parent
import pw.binom.io.file.read
import pw.binom.io.file.write
import pw.binom.io.use
import pw.binom.pool.ObjectPool
import pw.binom.toByteBufferUTF8
import pw.binom.io.file.File as BinomFile

/**
 * Really hoping that this can use Okio in the future because everything else is quite... bad.
 * [https://publicobject.com/2020/10/06/files/]
 */
class File(val delegate: BinomFile) {
  constructor(path: String) : this(BinomFile(path))

  constructor(parent: File, name: String) : this(
    BinomFile(
      parent = BinomFile(parent.path),
      name = name
    )
  )

  val exists: Boolean get() = delegate.isExist
  val path: String get() = delegate.path
  val name: String get() = delegate.name
  val parent: File? get() = delegate.parent?.let(::File)
  val isDirectory: Boolean get() = delegate.isDirectory

  companion object {
    val byteBufferPool = ByteBufferPool(10)
  }

  fun write(input: String) {
    parent?.let { check(it.exists) }

    val data: ByteBuffer = input.toByteBufferUTF8()
    delegate.write().use {
      it.write(data)
      it.flush()
    }
    data.clear()
  }

  fun read(): String {
    check(exists) { "Can't read non-existent: $path" }

    val buffer = byteBufferPool.borrow()
    try {
      // Bug workaround: Input.copyTo doesn't recycle borrowed buffers from the pool.
      val singleItemPool = object : ObjectPool<ByteBuffer> {
        override fun borrow(init: ((ByteBuffer) -> Unit)?) = buffer
        override fun recycle(value: ByteBuffer) = error("nope")
      }

      ByteArrayOutput().use { out ->
        delegate.read().use {
          it.copyTo(out, singleItemPool)
        }
        out.trimToSize()
        out.data.clear()
        return out.data.asUTF8String()
      }
    } finally {
      byteBufferPool.recycle(buffer)
    }
  }

  fun makeDirectory(recursively: Boolean = false) {
    if (recursively) {
      delegate.mkdirs()
    } else {
      delegate.mkdir()
    }
  }

  fun delete() {
    check(exists) { "$name does not exist: $path" }
    check(delegate.delete()) { "Failed to delete file: $this" }
  }

  fun sizeInBytes(): Long {
    check(exists)
    check(!isDirectory)
    return delegate.size
  }

  fun children(): List<File> {
    check(exists)
    check(delegate.isDirectory)

    val children = delegate.list()
    return children.map(::File)
  }

  /** @return the same [newFile] for convenience. */
  fun renameTo(newFile: File): File {
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

  /**
   * Like `content == read()`, but the plan is to avoid reading the whole file
   * into memory in the future when Okio supports native platforms.
   */
  fun equalsContent(content: String): Boolean {
    return read() == content
  }

  override fun toString(): String {
    return "${delegate.name} (${delegate.path})"
  }
}

val File.extension: String
  get() = name.substringAfterLast('.', "")

@OptIn(ExperimentalStdlibApi::class)
fun File.children(recursively: Boolean, skipDirectories: Boolean = true): List<File> {
  if (!recursively) return children()

  return buildList {
    for (child in children()) {
      if (!child.isDirectory || !skipDirectories) {
        add(child)
      }

      if (child.isDirectory) {
        addAll(child.children(recursively))
      }
    }
  }
}

fun File.delete(recursively: Boolean) {
  if (!recursively) {
    delete()
    return
  }

  if (isDirectory) {
    for (child in children()) {
      child.delete(recursively = true)
    }
  }
  delete()
}

fun File.relativePathIn(ancestor: File): String {
  check(path.contains(ancestor.path)) { "$ancestor does not contain $this" }
  return path.drop(ancestor.path.length + 1)  // +1 for the trailing "/".
}

fun File.touch(): File {
  parent?.makeDirectory(recursively = true)
  write("")
  return this
}

fun File?.existsOrNull(): File? {
  return if (this?.exists == true) this else null
}
