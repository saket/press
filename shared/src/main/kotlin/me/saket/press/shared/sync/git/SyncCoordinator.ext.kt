package me.saket.press.shared.sync.git

import com.badoo.reaktive.rxjavainterop.asRxJava2Completable
import io.reactivex.Completable
import me.saket.press.shared.sync.SyncCoordinator

fun SyncCoordinator.syncWithResultRx2(): Completable {
  return syncWithResult().asRxJava2Completable()
}
