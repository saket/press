package me.saket.press.shared.rx

import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.test.observable.assertValues
import com.badoo.reaktive.test.observable.test
import kotlin.test.Test

class PublishElementsTest {

  @Test fun `published stream is connected only after inner streams have subscribed`() {
    val events = observableOf(Unit)

    events
        .publishElements { sharedEvents ->
          merge(
              sharedEvents.map { "First" },
              sharedEvents.map { "Second" }
          )
        }
        .test()
        .assertValues("First", "Second")
  }
}
