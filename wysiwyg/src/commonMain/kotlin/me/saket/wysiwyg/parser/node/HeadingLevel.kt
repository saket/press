package me.saket.wysiwyg.parser.node

/**
 * @param textSizeRatio Taken from HTML5 spec: [http://zuga.net/articles/html-heading-elements/
 */
enum class HeadingLevel(val textSizeRatio: Float) {
  H1(2f),
  H2(1.5f),
  H3(1.17f),
  H4(1f),
  H5(.83f),
  H6(.75f);
}
