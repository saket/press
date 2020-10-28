package me.saket.press.shared.sync.git

interface File {
  companion object {
    // So that usages can use File() directly instead of PlatformFile().
    operator fun invoke(path: String): File = BinomBasedFile(path)
    operator fun invoke(parent: File, name: String): File = BinomBasedFile(parent, name)
  }

  val exists: Boolean
  val path: String
  val name: String
  val parent: File
  val isDirectory: Boolean

  val extension: String
    get() = name.substringAfterLast('.', "")

  val nameWithoutExtension: String
    get() = name.substringBeforeLast('.')

  fun write(input: String)

  fun read(): String

  fun makeDirectory(recursively: Boolean = false)

  fun delete()

  fun sizeInBytes(): Long

  fun children(): List<File>

  /** @return the same [newFile] for convenience. */
  fun renameTo(newFile: File): File

  /** Like `content == read()`, but avoids reading the whole file into memory when possible. */
  fun equalsContent(content: String): Boolean
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
  parent.makeDirectory(recursively = true)
  write("")
  return this
}

fun File?.existsOrNull(): File? {
  return if (this?.exists == true) this else null
}
