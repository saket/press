package press.widgets

import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE
import java.util.ArrayDeque
import java.util.Deque

/**
 * A [SpannableStringBuilder] wrapper whose API doesn't make me want to stab my eyes out.
 *
 * Copied from https://gist.github.com/JakeWharton/11274467
 */
class Truss {
  private val builder = SpannableStringBuilder()
  private val stack: Deque<Span> = ArrayDeque()

  fun append(charSequence: CharSequence) = apply { builder.append(charSequence) }

  /** Starts {@code span} at the current position in the builder. */
  fun pushSpan(span: Any) = apply { stack.addLast(Span(builder.length, span)) }

  /** End the most recently pushed span at the current position in the builder. */
  fun popSpan() = apply {
    val span = stack.removeLast()
    builder.setSpan(span.span, span.start, builder.length, SPAN_INCLUSIVE_EXCLUSIVE)
  }

  /** Create the final {@link CharSequence}, popping any remaining spans. */
  fun build(): CharSequence {
    while (stack.isNotEmpty()) {
      popSpan()
    }
    return builder // TODO make immutable copy?
  }

  private class Span(
    val start: Int,
    val span: Any?
  )
}

@JvmName("-buildSpannableString") // no Java please
inline fun buildSpannableString(block: Truss.() -> Unit): CharSequence =
  Truss().apply(block).build()
