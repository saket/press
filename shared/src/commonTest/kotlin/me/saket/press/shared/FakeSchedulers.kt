package me.saket.press.shared

import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.test.scheduler.TestScheduler
import me.saket.press.shared.rx.Schedulers

@Suppress("TestFunctionName")
fun FakeSchedulers(io: Scheduler = TestScheduler(), computation: Scheduler = TestScheduler()) =
  Schedulers(io, computation)
