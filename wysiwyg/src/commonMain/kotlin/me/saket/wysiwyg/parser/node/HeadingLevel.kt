package me.saket.wysiwyg.parser.node

/**
 * @param textSizeRatio Taken from HTML5 spec: [http://zuga.net/articles/html-heading-elements/
 */
enum class HeadingLevel(val textSizeRatio: Float) {
  H1(1.40f),
  H2(1.25f),
  H3(1.10f),
  H4(1f),
  H5(1f),
  H6(1f);
}
