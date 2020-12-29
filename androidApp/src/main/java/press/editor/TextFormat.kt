package press.editor

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import me.saket.wysiwyg.parser.MarkdownParser

enum class TextFormat {
  Markdown,
  Html;

  @Suppress("NAME_SHADOWING")
  fun generateFrom(text: CharSequence): Single<String> {
    val text = text.toString()
    return Single.fromCallable {
      when (this) {
        Markdown -> text
        Html -> MarkdownParser().renderHtml(text)
      }
    }.subscribeOn(Schedulers.io())
  }
}
