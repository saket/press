package me.saket.press.shared.sync.git

import io.reactivex.Completable
import me.saket.press.shared.sync.Syncer

fun Syncer.syncRx2() = Completable.fromAction { sync() }
