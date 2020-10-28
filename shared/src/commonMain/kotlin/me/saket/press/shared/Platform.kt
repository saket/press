package me.saket.press.shared

import kotlinx.coroutines.CoroutineScope

expect object Platform {
  val host: PlatformHost
}

@Suppress("EnumEntryName")
enum class PlatformHost {
  macOS,
  iOS,
  Android
}

expect fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T
