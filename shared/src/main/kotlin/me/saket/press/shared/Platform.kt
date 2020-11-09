package me.saket.press.shared

import android.os.Parcelable
import kotlinx.coroutines.CoroutineScope

actual object Platform {
  actual val host = PlatformHost.Android
}

actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T {
  return kotlinx.coroutines.runBlocking {
    block(this)
  }
}

actual typealias AndroidParcelize = kotlinx.android.parcel.Parcelize
actual interface AndroidParcel : Parcelable
