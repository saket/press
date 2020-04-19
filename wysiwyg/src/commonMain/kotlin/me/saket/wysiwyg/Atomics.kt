package me.saket.wysiwyg

/**
 * Common wrapper for atomicLazy() that is only available on native.
 * https://github.com/Kotlin/kotlinx.atomicfu/issues/135
 */
expect fun <T> atomicLazy(initializer: () -> T): Lazy<T>
