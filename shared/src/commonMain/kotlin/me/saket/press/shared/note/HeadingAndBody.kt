package me.saket.press.shared.note

data class HeadingAndBody(
  val heading: String,
  val body: String,
  val headingSyntax: String
) {

  companion object {
    private val regex = Regex("^(?:(\\s*#{1,6}[ \\t]+)(.*))?\\n?") // https://regexr.com/5btiv
    private val empty = HeadingAndBody("", "", "")

    fun parse(content: String, trimSpacings: Boolean = true): HeadingAndBody {
      val matchResult = regex.find(content) ?: return empty
      val (headingSyntax, heading) = matchResult.destructured
      val body =
        if (headingSyntax.isNotBlank()) content.substring(startIndex = matchResult.range.last + 1)
        else content

      return if (trimSpacings) {
        HeadingAndBody(heading.trimEnd(), body.trimStart(), headingSyntax.trim())
      } else {
        HeadingAndBody(heading, body, headingSyntax)
      }
    }

    fun prefixHeading(content: String, prefix: String): String {
      return with(parse(content, trimSpacings = false)) {
        buildString {
          append(headingSyntax)
          append(prefix)
          append(heading)
          if (heading.isNotBlank() && body.isNotBlank()) {
            append("\n")
          }
          append(body)
        }
      }
    }
  }
}
