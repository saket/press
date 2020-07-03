package press.extensions

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.MILLISECONDS

val Int.second get() = seconds

fun <T> Observable<T>.throttleFirst(
  timeSpan: TimeSpan,
  scheduler: Scheduler = Schedulers.computation()
) = throttleFirst(timeSpan.millisecondsLong, MILLISECONDS, scheduler)!!
