package compose.util

import com.soywiz.klock.TimeSpan
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.MILLISECONDS

fun <T> Observable<T>.throttleFirst(
  timeSpan: TimeSpan,
  scheduler: Scheduler = Schedulers.computation()
) = throttleFirst(timeSpan.millisecondsLong, MILLISECONDS, scheduler)!!