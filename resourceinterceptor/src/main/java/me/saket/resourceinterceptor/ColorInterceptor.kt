package me.saket.resourceinterceptor

typealias SystemColor = () -> Int?

class ColorInterceptor(val interceptor: (SystemColor) -> Int) {
  operator fun invoke(systemColor: SystemColor) = interceptor(systemColor)
}