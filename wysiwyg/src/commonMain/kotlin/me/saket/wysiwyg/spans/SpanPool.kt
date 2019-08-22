package me.saket.wysiwyg.spans

import me.saket.wysiwyg.WysiwygTheme
import me.saket.wysiwyg.util.Stack
import kotlin.DeprecationLevel.WARNING
import kotlin.reflect.KClass

typealias Recycler = (WysiwygSpan) -> Unit

class SpanPool(val theme: WysiwygTheme) {

  private val spans = mutableMapOf<KClass<*>, Stack<WysiwygSpan>>()
  val recycler: Recycler = this::recycle

  @Suppress("UNCHECKED_CAST")
  @Deprecated(message = "Use get<T>() instead", level = WARNING)
  fun <T : WysiwygSpan> get(
    clazz: KClass<T>,
    default: () -> T
  ): T {
    val similarSpans = spans.getOrElse(clazz) { Stack() }
    return when {
      similarSpans.isEmpty() -> default()
      else -> similarSpans.pop() as T
    }
  }

  /**
   * Offers `get<WysiwygSpan>()` instead of `get(WysiwygSpan::class.java)`.
   */
  @Suppress("DEPRECATION")
  inline fun <reified T : WysiwygSpan> get(noinline default: () -> T): T {
    return get(T::class, default)
  }

  fun recycle(span: WysiwygSpan) {
    val similarSpans = spans.getOrElse(span::class) { Stack() }
    similarSpans.add(span)
    spans[span::class] = similarSpans
  }
}