package press.extensions

import kotlin.LazyThreadSafetyMode.NONE

@Suppress("NOTHING_TO_INLINE")
inline fun <T> unsafeLazy(noinline initializer: () -> T): Lazy<T> {
  return lazy(NONE, initializer)
}
