package me.saket.press.shared.rx

import com.badoo.reaktive.scheduler.Scheduler

class Schedulers(
  val io: Scheduler,
  val computation: Scheduler
)
