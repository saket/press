package me.saket.press.shared

import com.badoo.reaktive.observable.map
import com.badoo.reaktive.rxjavainterop.asRxJava2Completable
import com.badoo.reaktive.rxjavainterop.asRxJava2Observable
import io.reactivex.Completable
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.preferences.Setting
import me.saket.press.shared.syncer.SyncCoordinator
import me.saket.press.shared.theme.AppTheme
import me.saket.press.shared.util.Optional
import me.saket.press.shared.util.toOptional
import com.badoo.reaktive.observable.Observable as ReaktiveObservable
import io.reactivex.Observable as RxJavaObservable

fun SyncCoordinator.syncWithResultRx2(): Completable {
  return syncWithResult().asRxJava2Completable()
}

fun EditorPresenter.saveEditorContentOnClose(content: String): Completable {
  return this.saveEditorContentOnClose(content).asRxJava2Completable()
}

fun <T : Any> Setting<T>.listen(): RxJavaObservable<Optional<T>> {
  val listen: ReaktiveObservable<T?> = this.listen()
  return listen
    .map { value -> value.toOptional() }
    .asRxJava2Observable()
}

fun AppTheme.listenRx() = listen().asRxJava2Observable()
