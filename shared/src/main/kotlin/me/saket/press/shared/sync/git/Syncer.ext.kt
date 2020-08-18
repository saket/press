package me.saket.press.shared.sync.git

import com.badoo.reaktive.rxjavainterop.asRxJava2Observable
import io.reactivex.Completable
import me.saket.press.shared.sync.Syncer

fun Syncer.statusRx2() = status().asRxJava2Observable()
fun Syncer.syncRx2() = Completable.fromAction { sync() }
