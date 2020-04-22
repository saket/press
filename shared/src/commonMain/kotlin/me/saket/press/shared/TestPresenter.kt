package me.saket.press.shared

import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.SingleWrapper
import com.badoo.reaktive.single.singleFromFunction
import com.badoo.reaktive.single.wrap

class TestPresenter {

  internal fun platformName(): Single<String> {
    return singleFromFunction { Platform.name }
  }
}
