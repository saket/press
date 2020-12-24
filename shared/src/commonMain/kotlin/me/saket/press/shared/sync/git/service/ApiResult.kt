package me.saket.press.shared.sync.git.service

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.publish
import me.saket.press.shared.sync.git.service.ApiResult.Failure
import me.saket.press.shared.sync.git.service.ApiResult.Success

sealed class ApiResult {
  object Success : ApiResult()
  data class Failure(val errorMessage: String?) : ApiResult()
}

fun Observable<ApiResult>.filterSuccess() = ofType<Success>()
fun Observable<ApiResult>.filterFailure() = ofType<Failure>()
