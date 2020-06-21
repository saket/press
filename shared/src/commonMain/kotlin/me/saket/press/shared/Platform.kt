package me.saket.press.shared

expect object Platform {
  val host: PlatformHost
}

@Suppress("EnumEntryName")
enum class PlatformHost {
  macOS,
  iOS,
  Android
}
