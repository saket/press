package me.saket.press.shared.rx

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isSameAs
import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.asObservable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.test.base.TestObserver
import me.saket.press.shared.rx.Notification.OnComplete
import me.saket.press.shared.rx.Notification.OnError
import me.saket.press.shared.rx.Notification.OnNext
import kotlin.reflect.KClass

/**
 * Unlike [TestObserver] that encourages assertions based on indices,
 * this forces each event in a stream to be consumed/asserted to prevent
 * accidental mistakes.
 *
 * Taken from https://github.com/square/retrofit.
 */
class RxRule {
  private val recorders = mutableListOf<Recorder<*>>()

  fun assertEmpty() {
    recorders.forEach { it.assertEmpty() }
  }

  fun <T> newObserver(): RecordingObserver<T> {
    return Recorder<T>().also {
      recorders += it
    }
  }
}

fun <T> Observable<T>.test(rxRule: RxRule): RecordingObserver<T> {
  val observer = rxRule.newObserver<T>()
  subscribe(observer)
  return observer
}

fun Completable.test(rxRule: RxRule): RecordingObserver<Unit> {
  val observer = rxRule.newObserver<Unit>()
  asObservable<Unit>().subscribe(observer)
  return observer
}

private class Recorder<T> : RecordingObserver<T> {
  private val events = ArrayDeque<Notification<T>>()
  private lateinit var disposable: Disposable

  override fun onSubscribe(disposable: Disposable) {
    this.disposable = disposable
  }

  override fun onNext(value: T) {
    events.add(OnNext(value))
  }

  override fun onComplete() {
    events.add(OnComplete)
  }

  override fun onError(error: Throwable) {
    error.printStackTrace()
    events.add(OnError(error))
  }

  private fun takeNotification(): Notification<T> {
    return events.removeFirstOrNull() ?: error("No event found!")
  }

  override fun popValue(): T {
    return takeNotification().let {
      check(it is OnNext) { "Expected onNext event but was $it" }
      it.value
    }
  }

  override fun popAllValues(): RecordingObserver<T> {
    while (events.firstOrNull() is OnNext) {
      popValue()
    }
    return this
  }

  override fun popError(): Throwable {
    return takeNotification().let {
      (it as? OnError)?.error ?: error("Expected onError event but was $it")
    }
  }

  override fun assertValue(value: T?): Recorder<T> {
    assertThat(popValue()).isEqualTo(value)
    return this
  }

  override fun assertComplete() {
    val notification = takeNotification()
    check(notification is OnComplete) { "Expected onCompleted event but was $notification" }
    assertEmpty() // Terminal event!
  }

  override fun assertError(throwable: Throwable) {
    assertThat(popError()).isSameAs(throwable)
    assertEmpty() // Terminal event!
  }

  override fun assertError(type: KClass<out Throwable>) {
    val throwable = popError()
    assertThat(throwable).isInstanceOf(type)
    assertEmpty() // Terminal event!
  }

  override fun assertEmpty(): Recorder<T> {
    assertThat(events, name = "Unconsumed events").isEmpty()
    return this
  }
}

interface RecordingObserver<T> : ObservableObserver<T> {
  fun assertEmpty(): RecordingObserver<T>

  fun popValue(): T
  fun popAllValues(): RecordingObserver<T>

  fun assertValue(value: T?): RecordingObserver<T>
  fun assertAnyValue() { popValue() }

  fun assertComplete()

  fun popError(): Throwable
  fun assertError(throwable: Throwable)
  fun assertError(type: KClass<out Throwable>)
}

private sealed class Notification<out T> {
  data class OnNext<out T>(val value: T) : Notification<T>()
  data class OnError(val error: Throwable) : Notification<Nothing>()
  object OnComplete : Notification<Nothing>()
}
