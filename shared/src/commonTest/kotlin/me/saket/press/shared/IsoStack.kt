package me.saket.press.shared

import co.touchlab.stately.collections.IsoMutableList

class IsoStack<T> {
  private val list = IsoMutableList<T>()

  fun pop(): T = list.removeLast()

  fun peek(): T? = list.removeLastOrNull()

  fun push(t: T) = list.add(list.lastIndex + 1, t)

  fun isEmpty(): Boolean = list.isEmpty()

  fun clear() = list.clear()
}
