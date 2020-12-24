package me.saket.press.shared

import kotlinx.coroutines.CoroutineScope

expect object Platform {
  val host: PlatformHost
}

@Suppress("EnumEntryName")
enum class PlatformHost {
  macOS,
  Android
}

expect fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class AndroidParcelize()

expect interface AndroidParcel
