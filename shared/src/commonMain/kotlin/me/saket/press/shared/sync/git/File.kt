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

  val path: String
  val name: String

  fun write(input: String)

  fun read(): String

  /** Kotlin-esque name for renaming a file. */
  fun copy(name: String): File

  fun makeDirectory()

  fun delete(recursively: Boolean = false)

  fun children(): List<File>
}
