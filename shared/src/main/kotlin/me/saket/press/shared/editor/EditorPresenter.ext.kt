package me.saket.press.shared.editor

import com.badoo.reaktive.rxjavainterop.asRxJava2Completable
import io.reactivex.Completable
import io.reactivex.annotations.CheckReturnValue

fun EditorPresenter.saveEditorContentOnClose(content: String): Completable =
  this.saveEditorContentOnClose(content).asRxJava2Completable()
