package me.saket.press.shared.note

import ch.tutteli.atrium.api.fluent.en_GB.hasSize
import ch.tutteli.atrium.api.verbs.expect
import com.badoo.reaktive.scheduler.trampolineScheduler
import me.saket.press.shared.settings.FakeSetting
import kotlin.test.Test

class PrePopulatedNotesTest {

  private val setting = FakeSetting(PrePopulatedNotesInserted(false))
  private val repository = FakeNoteRepository()

  private val prePopulatedNotes = PrePopulatedNotes(
      setting = setting,
      repository = repository,
      ioScheduler = trampolineScheduler
  )

  @Test fun `notes are pre-populated on first app launch`() {
    setting.set(PrePopulatedNotesInserted(false))

    expect(repository.savedNotes).hasSize(0)

    prePopulatedNotes.doWork()
    expect(repository.savedNotes).hasSize(3)
  }

  @Test fun `notes aren't pre-populated on subsequent app launches`() {
    setting.set(PrePopulatedNotesInserted(true))

    prePopulatedNotes.doWork()
    expect(repository.savedNotes).hasSize(0)
  }
}
