package me.saket.press.shared.sync.git

expect class PlatformFile(parentPath: String, name: String) : File

interface File {
  companion object {
    // So that usages can use File() directly instead of PlatformFile().
    operator fun invoke(parentPath: String, name: String) = PlatformFile(parentPath, name)
  }

  val path: String

  fun write(input: String)

  /** Kotlin-esque name for renaming a file. */
  fun copy(name: String): File

  fun makeDirectory()
}
