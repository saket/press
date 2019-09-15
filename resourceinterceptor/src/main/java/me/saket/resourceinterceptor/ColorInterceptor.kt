package me.saket.resourceinterceptor

typealias SystemColor = () -> Int

interface ColorInterceptor {
  fun intercept(systemColor: SystemColor): Int
}

fun ColorInterceptor(interceptor: (SystemColor) -> Int): ColorInterceptor {
  return object : ColorInterceptor {
    override fun intercept(systemColor: SystemColor) = interceptor(systemColor)
  }
}