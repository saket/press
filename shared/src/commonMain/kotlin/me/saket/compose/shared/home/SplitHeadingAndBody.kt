package me.saket.compose.shared.home

object SplitHeadingAndBody {

  fun split(content: String): Pair<String, String> {
    val trimmedContent = content.trimStart()

    val title: String
    val body: String

    if (trimmedContent.startsWith("#")) {
      val titleStartIndex = trimmedContent.indexOfFirst { it.isWhitespace() } + 1
      val titleAndBodySeparatorIndex = trimmedContent.indexOf('\n')
      val hasBody = titleAndBodySeparatorIndex != -1

      if (hasBody) {
        title = trimmedContent.substring(titleStartIndex, titleAndBodySeparatorIndex)
        body = trimmedContent.substring(titleAndBodySeparatorIndex + 1).trimStart()

      } else {
        title = trimmedContent.substring(titleStartIndex)
        body = ""
      }

    } else {
      title = ""
      body = trimmedContent
    }

    return title to body
  }
}