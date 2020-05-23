package me.saket.press.shared.sync

import com.badoo.reaktive.completable.Completable

interface Syncer {
  fun startUserAuth(): Completable
}
