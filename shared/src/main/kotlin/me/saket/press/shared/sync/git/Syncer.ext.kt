package me.saket.press.shared.sync.git

import com.badoo.reaktive.rxjavainterop.asRxJava2Completable
import com.badoo.reaktive.rxjavainterop.asRxJava2Observable
import me.saket.press.shared.sync.Syncer

fun Syncer.statusRx2() = status().asRxJava2Observable()
fun Syncer.syncRx2() = sync().asRxJava2Completable()
