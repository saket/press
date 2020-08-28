package me.saket.press.shared.sync.git

actual class PlatformFile actual constructor(parentPath: String, name: String) : File {
  override val path: String get() = TODO()
  override val name: String get() = TODO()
  override val exists: Boolean get() = TODO()
  override val parent: File? get() = TODO()
  override val isDirectory: Boolean get() = TODO()

  actual constructor(path: String) : this("foo", "bar") { TODO() }

  override fun write(input: String): Unit = TODO()
  override fun read(): String = TODO()
  override fun copy(name: String): File = TODO()
  override fun renameTo(newFile: File): File = TODO()
  override fun equalsContent(content: String): Boolean = TODO()
  override fun makeDirectory(recursively: Boolean): Unit = TODO()
  override fun delete(): Unit = TODO()
  override fun children(): List<File> = TODO()
}
