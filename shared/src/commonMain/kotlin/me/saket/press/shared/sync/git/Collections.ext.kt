package me.saket.press.shared.sync.git

@OptIn(ExperimentalStdlibApi::class)
internal fun <E> List<E>.zipWithNext(initialValue: E?): List<Pair<E?, E>> {
  return buildList {
    add(initialValue)
    addAll(this@zipWithNext)
  }.zipWithNext(transform = { a, b -> a to b!! })
}
