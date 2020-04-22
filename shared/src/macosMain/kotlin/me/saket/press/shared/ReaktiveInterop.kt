package me.saket.press.shared

import com.badoo.reaktive.single.SingleWrapper
import com.badoo.reaktive.single.wrap

fun TestPresenter.platformNameWrapper(): SingleWrapper<String> {
  return platformName().wrap()
}
