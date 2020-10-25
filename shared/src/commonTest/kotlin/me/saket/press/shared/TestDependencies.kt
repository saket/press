package me.saket.press.shared

import kotlinx.coroutines.CoroutineScope
import me.saket.press.shared.sync.git.DeviceInfo

expect fun testDeviceInfo(): DeviceInfo

expect fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T
