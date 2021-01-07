package me.saket.press.shared.util

import kotlin.jvm.JvmSynthetic

/**
 * Copied from https://github.com/gojuno/koptional
 */
sealed class Optional<out T : Any> {
  /**
   * Converts [Optional] to either its non-null value if it's [Some] or `null` if it's [None].
   */
  abstract fun toNullable(): T?

  /**
   * Unwraps this optional into the value it holds or null if there is no value held.
   */
  @JvmSynthetic
  abstract operator fun component1(): T?
}

data class Some<out T : Any>(val value: T) : Optional<T>() {
  override fun toString() = "Some($value)"
  override fun toNullable(): T = value
}

object None : Optional<Nothing>() {
  override fun toString() = "None"

  override fun component1(): Nothing? = null

  override fun toNullable(): Nothing? = null
}

/**
 * Wraps an instance of T (or null) into an [Optional]:
 *
 * ```kotlin
 * val a: String? = "str"
 * val b: String? = null
 *
 * val optionalA = a.toOptional() // Some("str")
 * val optionalB = b.toOptional() // None
 * ```
 *
 * This is the preferred method of obtaining an instance of [Optional] in Kotlin. In Java, prefer
 * using the static [Optional.toOptional] method.
 */
fun <T : Any> T?.toOptional(): Optional<T> = if (this == null) None else Some(this)
