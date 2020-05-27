package me.saket.press.shared.sync.git

import okio.buffer
import okio.sink
import java.io.File as JavaFile

actual class PlatformFile actual constructor(
  private val parentPath: String,
  name: String
) : File {
  private val delegate = JavaFile(parentPath, name)

  override val path: String
    get() = delegate.path

  override fun write(input: String) {
    delegate.sink().buffer().use {
      it.writeUtf8(input)
    }
  }

  override fun copy(name: String): File {
    val renamed = delegate.renameTo(JavaFile(parentPath, name))
    check(renamed) { "Couldn't rename file ($this) to $name" }
    return PlatformFile(parentPath, name)
  }

  override fun makeDirectory() {
    delegate.mkdir()
  }
}
