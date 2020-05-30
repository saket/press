package me.saket.press.shared.sync.git

import okio.buffer
import okio.sink
import java.io.File as JavaFile

actual class PlatformFile constructor(private val delegate: JavaFile) : File {
  actual constructor(parentPath: String, name: String): this(JavaFile(parentPath, name))
  actual constructor(path: String): this(JavaFile(path))

  override val path: String
    get() = delegate.path

  override fun write(input: String) {
    delegate.sink().buffer().use {
      it.writeUtf8(input)
    }
  }

  override fun copy(name: String): File {
    require(delegate.parent != null)
    val renamedDelegate = JavaFile(delegate.parent, name)
    val renamed = delegate.renameTo(renamedDelegate)
    check(renamed) { "Couldn't rename file ($this) to $name" }
    return PlatformFile(renamedDelegate)
  }

  override fun makeDirectory() {
    delegate.mkdir()
  }

  override fun delete(recursively: Boolean) {
    if (recursively) {
      delegate.deleteRecursively()
    } else {
      delegate.delete()
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
}
