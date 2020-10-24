package me.saket.press.shared.sync.git

import me.saket.press.shared.util.Locale
import me.saket.press.shared.util.isLetterOrDigit
import me.saket.press.shared.util.toLowerCase

object FileNameSanitizer {
  /**
   * Filters out letters that aren't safe to use for a file name.
   *
   * @param maxLength 255 is the limit on both Unix and NTFS.
   */
  fun sanitize(string: String, maxLength: Int = 255): String {
    // Found these rules here: https://stackoverflow.com/a/458001/2511884
    return string.toLowerCase(Locale.US)
      .replace(" ", "_")
      .replace(".", "_")
      .replace("-", "_")
      .filter { char -> char.isLetterOrDigit() || char == '_' }
      .take(maxLength)
  }
}
