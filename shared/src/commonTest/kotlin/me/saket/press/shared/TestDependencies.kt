package me.saket.press.shared

import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.sync.git.DeviceInfo
import kotlin.reflect.KClass

/**
 * Common tests are run on the JVM using Robolectric so that
 * an in-memory database can be created. See [BaseDatabaeTest].
 */
expect abstract class RobolectricTest()

expect fun testDeviceInfo(): DeviceInfo
