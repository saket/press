package me.saket.press.shared.syncer.git

import com.badoo.reaktive.rxjavainterop.asRxJava2Completable
import io.reactivex.Completable
import me.saket.press.shared.syncer.SyncCoordinator

fun SyncCoordinator.syncWithResultRx2(): Completable {
  return syncWithResult().asRxJava2Completable()
}
