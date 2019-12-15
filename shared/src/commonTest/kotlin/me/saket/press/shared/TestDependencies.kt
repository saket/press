package me.saket.press.shared

import me.saket.press.shared.db.BaseDatabaeTest
import kotlin.reflect.KClass

/**
 * Still not fully sure how this works, but the common tests are on the JVM
 * using Android's JUnit runner so that an in-memory database can be created.
 * See [BaseDatabaeTest].
 *
 * Copied from https://github.com/russhwolf/soluna
 */
expect abstract class Runner
expect class AndroidJUnit4 : Runner

@Suppress("unused")
@OptionalExpectation
@UseExperimental(ExperimentalMultiplatform::class)
expect annotation class RunWith(val value: KClass<out Runner>)
