package me.saket.kgit

import java.io.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.io.use as kotlinUse

@OptIn(ExperimentalContracts::class)
internal inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
  contract { callsInPlace(block, EXACTLY_ONCE) }

  val closeable = Closeable { close() }
  return closeable.kotlinUse {
    block(this)
  }
}
