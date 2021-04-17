package me.saket.wysiwyg

import kotlin.LazyThreadSafetyMode.NONE

actual fun <T> atomicLazy(initializer: () -> T): Lazy<T> = lazy(NONE, initializer)
