package me.saket.press.shared

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject

data class DeepLink(val url: String)

interface DeepLinks {
  fun broadcast(link: DeepLink)
  fun listen(): Observable<DeepLink>
}

class RealDeepLinks : DeepLinks {
  private val links = PublishSubject<DeepLink>()
  override fun broadcast(link: DeepLink) = links.onNext(link)
  override fun listen(): Observable<DeepLink> = links
}
