package me.saket.press.shared.home

object SplitHeadingAndBody {

  private val headingRegex = Regex("^#{1,6}[ \t]+")

  fun split(content: String): Pair<String, String> {
    @Suppress("NAME_SHADOWING")
    val content = content.trimStart()

    val title: String
    val body: String

    if (headingRegex.containsMatchIn(content)) {
      val titleStartIndex = content.indexOfFirst { it.isWhitespace() } + 1
      val titleAndBodySeparatorIndex = content.indexOf('\n')
      val hasBody = titleAndBodySeparatorIndex != -1

      if (hasBody) {
        title = content.substring(titleStartIndex, titleAndBodySeparatorIndex).trimEnd()
        body = content.substring(titleAndBodySeparatorIndex + 1).trimStart()

      } else {
        title = content.substring(titleStartIndex).trimEnd()
        body = ""
      }

    } else {
      title = ""
      body = content
    }

    return title to body
  }
}
