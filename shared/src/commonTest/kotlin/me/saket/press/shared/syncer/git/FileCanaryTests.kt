package me.saket.press.shared.syncer.git

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import me.saket.press.shared.containsOnly
import me.saket.press.shared.testDeviceInfo
import kotlin.test.AfterTest
import kotlin.test.Test

class FileCanaryTests {
  private val storage = testDeviceInfo().appStorage

  companion object {
    val content = """
      |I can't do that as Bruce Wayne... as a man. I'm flesh and blood. 
      |I can be ignored, destroyed. But as a symbol, I can be incorruptible, 
      |I can be everlasting. Bats frighten me. It's time my enemies shared my dread.
      """.trimMargin().repeat(10)
  }

  @AfterTest
  fun cleanup() {
    storage.delete(recursively = true)
  }

  @Test fun `read and write`() {
    val file = File(storage, "batman.md")
    file.write(content)
    assertThat(file.read()).isEqualTo(content)
  }

  @Test fun name() {
    val file = File("batman.md")
    assertThat(file.name).isEqualTo("batman.md")
  }

  @Test fun paths() {
    val file = File(storage, "archive/batman.md").makeDirectories()
    assertThat(file.path).isEqualTo(storage.path + "/archived/batman.md")
    assertThat(file.relativePathIn(storage)).isEqualTo("archive/batman.md")
  }

  @Test fun delete() {
    val file = File(storage, "batman.md")

    file.touch()
    assertThat(file.exists).isTrue()

    file.delete()
    assertThat(file.exists).isFalse()
  }

  @Test fun rename() {
    val file = File(storage, "batman.md")
    file.write(content)

    val archived = File(storage, "archive").makeDirectories()
    val renamedFile = file.renameTo(File(archived, "batman.md"))

    assertThat(file.exists).isFalse()
    assertThat(renamedFile.exists).isTrue()
    assertThat(renamedFile.read()).isEqualTo(content)
    assertThat(renamedFile.relativePathIn(storage)).isEqualTo("archive/batman.md")
  }

  @Test fun parent() {
    val file = File(storage, "archive/batman.md")
    assertThat(file.relativePathIn(storage)).isEqualTo("archive/batman.md")
    assertThat(file.parent.relativePathIn(storage)).isEqualTo("archive")
  }

  @Test fun `make directories`() {
    val archived = File(storage, "archive")
    archived.makeDirectories()

    assertThat(archived.exists).isTrue()
    assertThat(archived.isDirectory).isTrue()

    val deepFolder = File(storage, "blog/android/text")
    deepFolder.makeDirectories()
    assertThat(File(storage, "blog/android/text").exists).isTrue()
  }

  @Test fun `equals content`() {
    val file1 = File(storage, "batman.md").apply { write(content) }
    assertThat(file1.equalsContent(content)).isTrue()

    val file2 = File(storage, "batman.md").apply { write("foo") }
    assertThat(file2.equalsContent(content)).isFalse()
  }

  @Test fun `recursive children`() {
    File(storage, "note_1.md").touch()
    File(storage, "note_2.md").touch()
    File(storage, "note_3.md").touch()

    val archived = File(storage, "archive").apply { makeDirectories() }
    File(archived, "note_3.md").touch()
    File(archived, "note_4.md").touch()
    File(archived, "note_5.md").touch()

    assertThat(storage.children(recursively = false).map { it.relativePathIn(storage) })
      .containsOnly(
        "note_1.md",
        "note_2.md",
        "note_3.md",
        "archive"
      )
    assertThat(storage.children(recursively = true, skipDirectories = true).map { it.relativePathIn(storage) })
      .containsOnly(
        "note_1.md",
        "note_2.md",
        "note_3.md",
        "archive/note_3.md",
        "archive/note_4.md",
        "archive/note_5.md"
      )
    assertThat(storage.children(recursively = true, skipDirectories = false).map { it.relativePathIn(storage) })
      .containsOnly(
        "note_1.md",
        "note_2.md",
        "note_3.md",
        "archive",
        "archive/note_3.md",
        "archive/note_4.md",
        "archive/note_5.md"
      )
    assertThat(archived.children(recursively = true).map { it.relativePathIn(archived) })
      .containsOnly(
        "note_3.md",
        "note_4.md",
        "note_5.md"
      )
  }
}
