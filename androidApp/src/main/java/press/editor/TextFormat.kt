package press.editor

enum class TextFormat {
  Markdown,
  Html;

  fun generateFrom(text: CharSequence): CharSequence {
    return when (this) {
      Markdown -> text
      Html -> TODO("Convert markdown to HTML")
    }
  }
}
