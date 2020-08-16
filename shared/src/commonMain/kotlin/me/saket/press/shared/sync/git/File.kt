package me.saket.press.shared.sync.git

expect class PlatformFile(parentPath: String, name: String) : File {
  constructor(path: String)
}

interface File {
  companion object {
    // So that usages can use File() directly instead of PlatformFile().
    operator fun invoke(path: String): File = PlatformFile(path)
    operator fun invoke(parent: File, name: String): File = PlatformFile(parent.path, name)
    operator fun invoke(parentPath: String, name: String): File = PlatformFile(parentPath, name)
  }

  val exists: Boolean
  val path: String
  val name: String
  val parent: File?
  val isDirectory: Boolean

  val extension: String
    get() = name.substringAfterLast('.', "")

  val nameWithoutExtension: String
    get() = name.substringBeforeLast('.')

  fun write(input: String)

  fun read(): String

  fun copy(name: String, recursively: Boolean = false): File

  fun makeDirectory(recursively: Boolean = false)

  fun delete(recursively: Boolean = false)

  fun children(): List<File>

  @OptIn(ExperimentalStdlibApi::class)
  fun children(recursively: Boolean): List<File> {
    if (!recursively) return children()

    return buildList {
      for (child in children()) {
        add(child)
        if (child.isDirectory) {
          addAll(child.children(recursively))
        }
      }
    }
  }

  fun relativePathIn(ancestor: File): String {
    check(path.contains(ancestor.path)) { "$ancestor does not contain $this" }
    return path.drop(ancestor.path.length + 1)  // +1 for the trailing "/".
  }

  /** @return the same [newFile] for convenience. */
  fun renameTo(newFile: File): File

  fun renameTo(newName: String): File {
    return renameTo(newFile = File(parent!!, newName))
  }
}

fun File.touch(): File {
  parent?.let { if (!exists) it.makeDirectory(recursively = true) }
  write("")
  return this
}
