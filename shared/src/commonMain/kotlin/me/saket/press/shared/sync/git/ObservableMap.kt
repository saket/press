package me.saket.press.shared.sync.git

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.startWithValue
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import com.badoo.reaktive.subject.publish.PublishSubject

class ObservableMutableMap<K, V> {
  private val delegate = mutableMapOf<K, V>()
  private val broadcaster = BehaviorSubject<Map<K, V>>(delegate)

  operator fun get(key: K) = delegate[key]

  operator fun set(key: K, value: V) {
    delegate[key] = value
    broadcaster.onNext(delegate)
  }

  fun listen(): Observable<Map<K, V>> = broadcaster
}
