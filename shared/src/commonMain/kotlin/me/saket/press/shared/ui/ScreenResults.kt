package me.saket.press.shared.ui

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.subject.publish.PublishSubject

class ScreenResults {
  private val results = PublishSubject<ScreenResult>()

  fun broadcast(result: ScreenResult) {
    results.onNext(result)
  }

  internal inline fun <reified T : ScreenResult> listen(): Observable<T> {
    return results.ofType()
  }
}

interface ScreenResult
