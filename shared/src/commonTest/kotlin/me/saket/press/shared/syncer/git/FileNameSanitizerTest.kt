package me.saket.press.shared.syncer.git

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class FileNameSanitizerTest {

  @Test fun `capital letters`() {
    val name = FileNameSanitizer.sanitize("Nicolas Cage")
    assertThat(name).isEqualTo("nicolas_cage")
  }

  @Test fun `non-english letters`() {
    val name = FileNameSanitizer.sanitize("Benoît's Diãry")
    assertThat(name).isEqualTo("benoîts_diãry")
  }
}
