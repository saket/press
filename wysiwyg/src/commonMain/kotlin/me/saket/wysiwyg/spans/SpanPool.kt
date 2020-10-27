package me.saket.wysiwyg.spans

import kotlin.DeprecationLevel.WARNING
import kotlin.reflect.KClass

typealias Recycler = (WysiwygSpan) -> Unit

/**
 * Allocating spans on every text change can be
 * expensive so Wysiwyg recycles the spans using SpanPool.
 */
class SpanPool {

  @Suppress("LeakingThis")
  private val recycler: Recycler = this::recycle
  private val spans = mutableMapOf<KClass<*>, Stack<WysiwygSpan>>()

  @Suppress("UNCHECKED_CAST")
  @Deprecated(message = "Use get<T>() instead", level = WARNING)
  fun <T : WysiwygSpan> get(
    clazz: KClass<T>,
    default: (Recycler) -> T
  ): T {
    val similarSpans = spans.getOrElse(clazz) { Stack() }
    return when {
      similarSpans.isEmpty() -> default(recycler)
      else -> similarSpans.pop() as T
    }
  }

  /**
   * Offers `get<WysiwygSpan>()` instead of `get(WysiwygSpan::class.java)`.
   */
  @Suppress("DEPRECATION")
  inline fun <reified T : WysiwygSpan> get(noinline default: (Recycler) -> T): T {
    return get(T::class, default)
  }

  fun recycle(span: WysiwygSpan) {
    val similarSpans = spans.getOrElse(span::class) { Stack() }
    similarSpans.push(span)
    spans[span::class] = similarSpans
  }
}

interface Stack<T> {
  fun pop(): T
  fun push(t: T)
  fun isEmpty(): Boolean

  companion object {
    operator fun <T> invoke(): Stack<T> {
      return object : Stack<T> {
        private val list = mutableListOf<T>()
        override fun pop(): T = list.removeLast()
        override fun push(t: T) = list.add(list.lastIndex + 1, t)
        override fun isEmpty(): Boolean = list.isEmpty()
      }
    }
  }
}
