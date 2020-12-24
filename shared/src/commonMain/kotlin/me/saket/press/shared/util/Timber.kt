package me.saket.press.shared.util

import co.touchlab.stately.collections.IsoMutableList

object Timber {
  private val loggers = IsoMutableList<Tree>()

  fun plant(tree: Tree) {
    loggers.add(tree)
  }

  fun i(message: String) {
    loggers.forEach { it.i(message) }
  }

  fun e(e: Throwable, message: String) {
    loggers.forEach { it.e(e, message) }
  }

  interface Tree {
    fun i(message: String)
    fun e(e: Throwable, message: String)
  }
}
