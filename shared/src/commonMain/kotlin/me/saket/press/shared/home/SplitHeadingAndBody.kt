package me.saket.press.shared.home

object SplitHeadingAndBody {
  // https://regexr.com/5btiv
  private val regex = Regex("^(?:(\\s*#{1,6}[ \\t]+)(.*))?\\n*([\\s\\S]*)")

  fun split(content: String, trimSpacings: Boolean = true): Paragraphs {
    val matchResult = regex.find(content)
        ?: return Paragraphs(
            heading = "",
            body = "",
            headingSyntax = ""
        )

    val (headingSyntax, heading, body) = matchResult.destructured
    return if (trimSpacings) {
      Paragraphs(heading.trimEnd(), body.trimStart(), headingSyntax.trim())
    } else {
      Paragraphs(heading, body, headingSyntax)
    }
  }
}

data class Paragraphs(
  val heading: String,
  val body: String,
  val headingSyntax: String
)
