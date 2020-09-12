package me.saket.press.shared.ui

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value

class FakeNavigator : Navigator {
  private val backstack = AtomicReference<ScreenKey?>(null)

  override fun lfg(screen: ScreenKey) {
    backstack.set(screen)
  }

  fun pop() = backstack.value ?: error("backstack is empty")
}
