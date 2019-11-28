package me.saket.press.shared.note

import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.scheduler.Scheduler
import com.benasher44.uuid.Uuid
import com.russhwolf.settings.ExperimentalListener
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

@Suppress("PrivatePropertyName")
class PrePopulatedNotes(
  private val settings: ObservableSettings,
  private val repository: NoteRepository,
  private val ioScheduler: Scheduler
) {

  /**
   * @param uuid UUIDs are hardcoded to ensure they remain the same for everyone. It's important
   * that they get merged and not duplicated when notes are synced between multiple devices.
   */
  private val WELCOME = InsertNote(
      uuid = Uuid.parse("e731d56f-8db6-4351-a05e-8df27d5086f0")!!,
      content = """
      |# Welcome to Press
      |Press is a cross-platform app for crafting notes, inspired by [Bear](bear.app) written using Kotlin Multiplatform.
    """.trimMargin()
  )

  private val TIPS_AND_TRICKS = InsertNote(
      uuid = Uuid.parse("8f4192fd-38cf-4c15-8ac7-028d15fe5fc3")!!,
      content = """
        |# Tips and tricks
        |Press supports these markdown syntax:
        |- Bold
        |- Italic
        |- Strikethrough
        |- Headings
        |- Quote
        |- Horizontal rule
        |- Lists
        |- Links
        |- Code
      """.trimMargin()
  )

  private val HOW_TO_CONTRIBUTE = InsertNote(
      uuid = Uuid.parse("b17bf2a8-6faf-4cc7-9c6a-d526a48c2530")!!,
      content = """
        |# How to contribute
        |Press is a community built app. For feedback and code contribution, checkout its [Github](https://github.com/saket/press) page.
      """.trimMargin()
  )

  fun doWork() {
    val welcomeNotesPopulated = settings.get("prepopulated_notes_inserted", defaultValue = false)
    if (welcomeNotesPopulated.not()) {
      repository.create(WELCOME, TIPS_AND_TRICKS, HOW_TO_CONTRIBUTE)
          .subscribeOn(ioScheduler)
          .subscribe {
            settings["prepopulated_notes_inserted"] = true
          }
    }
  }
}
