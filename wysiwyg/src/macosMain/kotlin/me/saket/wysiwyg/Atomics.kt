package me.saket.wysiwyg

actual fun <T> atomicLazy(initializer: () -> T): Lazy<T> = kotlin.native.concurrent.atomicLazy(initializer)
