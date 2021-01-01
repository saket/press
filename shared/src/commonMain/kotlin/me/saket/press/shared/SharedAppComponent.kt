package me.saket.press.shared

import com.badoo.reaktive.scheduler.computationScheduler
import com.badoo.reaktive.scheduler.ioScheduler
import me.saket.press.shared.di.koin
import me.saket.press.shared.keyboard.KeyboardShortcuts
import me.saket.press.shared.keyboard.RealKeyboardShortcuts
import me.saket.press.shared.localization.ENGLISH_STRINGS
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.syncer.SyncCoordinator
import me.saket.press.shared.time.Clock
import me.saket.press.shared.time.RealClock
import me.saket.press.shared.ui.ScreenResults
import org.koin.dsl.module

class SharedAppComponent {
  val module = module {
    single { ENGLISH_STRINGS }
    single<KeyboardShortcuts> { RealKeyboardShortcuts() }
    single<Clock> { RealClock() }
    single { ScreenResults() }
    factory { Schedulers(io = ioScheduler, computation = computationScheduler) }
  }

  companion object {
    fun strings(): Strings = koin()
    fun keyboardShortcuts(): KeyboardShortcuts = koin()
    fun syncCoordinator(): SyncCoordinator = koin()
    fun screenResults(): ScreenResults = koin()
  }
}
