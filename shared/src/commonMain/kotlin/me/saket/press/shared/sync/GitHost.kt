package me.saket.press.shared.sync

import com.badoo.reaktive.completable.Completable

interface GitHost {
  fun createAuthorizationRequestUrl(): String
  fun completeAuthorization(callbackUrl: String): Completable
}
