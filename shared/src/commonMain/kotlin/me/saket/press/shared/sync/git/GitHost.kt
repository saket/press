package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.Completable

interface GitHost {
  fun authorizationRequestUrl(): String
  fun completeAuthorization(callbackUrl: String): Completable
}
