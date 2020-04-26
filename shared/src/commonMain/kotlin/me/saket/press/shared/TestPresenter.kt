package me.saket.press.shared

import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.singleFromFunction

class TestPresenter {

  internal fun platformName(): Single<String> {
    return singleFromFunction { Platform.name }
  }
}
