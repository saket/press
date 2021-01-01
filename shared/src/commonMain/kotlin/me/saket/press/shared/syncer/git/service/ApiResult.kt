package me.saket.press.shared.syncer.git.service

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ofType
import me.saket.press.shared.syncer.git.service.ApiResult.Failure
import me.saket.press.shared.syncer.git.service.ApiResult.Success

sealed class ApiResult<out T> {
  data class Success<T>(val result: T) : ApiResult<T>()
  data class Failure(val errorMessage: String?) : ApiResult<Nothing>()
}

fun <T> Observable<ApiResult<T>>.filterSuccess() = ofType<Success<T>>()
fun Observable<ApiResult<*>>.filterFailure() = ofType<Failure>()
