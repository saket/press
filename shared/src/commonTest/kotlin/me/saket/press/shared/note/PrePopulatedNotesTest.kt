package me.saket.press.shared.note

import com.badoo.reaktive.scheduler.trampolineScheduler
import com.russhwolf.settings.MockSettings
import com.russhwolf.settings.set
import org.amshove.kluent.shouldHaveSize
import kotlin.test.Test

class PrePopulatedNotesTest {

  private val settings = MockSettings()
  private val repository = FakeNoteRepository()

  private val prePopulatedNotes = PrePopulatedNotes(
      settings,
      repository = repository,
      ioScheduler = trampolineScheduler
  )

  @Test fun `notes are pre-populated on first app launch`() {
    settings["prepopulated_notes_inserted"] = false

    repository.savedNotes shouldHaveSize 0

    prePopulatedNotes.doWork()
    repository.savedNotes shouldHaveSize 3
  }

  @Test fun `notes aren't pre-populated on subsequent app launches`() {
    settings["prepopulated_notes_inserted"] = true

    prePopulatedNotes.doWork()
    repository.savedNotes shouldHaveSize 0
  }
}