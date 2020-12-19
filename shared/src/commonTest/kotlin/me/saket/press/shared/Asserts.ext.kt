package me.saket.press.shared

import assertk.Assert
import assertk.assertions.support.expected

fun Assert<Collection<*>?>.isEqualTo(other: Collection<*>) = containsOnly(*other.toTypedArray())

/**
 * Prints a readable error message, unlike assertk.
 */
fun Assert<Iterable<*>?>.containsOnly(vararg elements: Any?) = given { actual ->
  val notInActual = elements.filterNot { it in (actual ?: emptyList<Any>()) }
  val notInExpected = actual?.filterNot { it in elements } ?: emptyList()
  if (notInExpected.isEmpty() && notInActual.isEmpty()) {
    return
  }

  expected(
    buildString {
      append("to contain only:")
      elements.toList().appendFlatString(this)

      append("\n\nBut was:")
      if (actual == null) append(" null")
      else actual.appendFlatString(this)

      if (notInActual.isNotEmpty()) {
        append("\n\nElements not found:")
        notInActual.appendFlatString(this)
      }
      if (notInExpected.isNotEmpty()) {
        append("\n\nExtra elements found:")
        notInExpected.appendFlatString(this)
      }
    }
  )
}

private fun Iterable<*>.appendFlatString(builder: StringBuilder) {
  forEach {
    builder.append("\n")
    builder.append(it?.toString()?.replace("\n", "\\n"))
  }
}
