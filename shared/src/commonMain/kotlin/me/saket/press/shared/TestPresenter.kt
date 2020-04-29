package me.saket.press.shared

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.doOnBeforeComplete
import com.badoo.reaktive.observable.doOnBeforeDispose
import com.badoo.reaktive.observable.doOnBeforeFinally
import com.badoo.reaktive.observable.doOnBeforeSubscribe
import com.badoo.reaktive.observable.observableDefer
import com.badoo.reaktive.observable.observableInterval
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.observable.observableTimer
import com.badoo.reaktive.observable.observableUnsafe
import com.badoo.reaktive.observable.wrap
import com.badoo.reaktive.observable.zipWith
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.singleFromFunction
import com.soywiz.klock.seconds

class TestPresenter {

  fun streamWithImmediateValues(): ObservableWrapper<String> {
    return observableOf("one", "two", "three").wrap()
  }

  fun streamWithAsyncValues(): ObservableWrapper<String> {
    return streamWithImmediateValues()
        .zipWith(observableInterval(1_000, ioScheduler)) { value, _ -> value }
        .wrap()
  }

  internal fun platformName(): Single<String> {
    return singleFromFunction { Platform.name }
  }
}
