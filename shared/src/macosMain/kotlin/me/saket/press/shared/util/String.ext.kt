@file:Suppress("NOTHING_TO_INLINE")

package me.saket.press.shared.util

import platform.Foundation.NSString
import platform.Foundation.lowercaseString

@Suppress("CAST_NEVER_SUCCEEDS")  // https://youtrack.jetbrains.com/issue/KT-30959
actual inline fun String.toLowerCase(locale: Locale): String {
  require(locale == Locale.US)
  return (this as NSString).lowercaseString()
}

actual fun String.format(vararg args: Any): String {
  TODO()
//  return NSString.stringWithFormat(this, args)
}
