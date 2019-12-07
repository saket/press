package me.saket.press.shared.note

import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.scheduler.Scheduler
import com.benasher44.uuid.Uuid
import me.saket.press.shared.settings.Setting

data class PrePopulatedNotesInserted(val inserted: Boolean)

@Suppress("PrivatePropertyName")
class PrePopulatedNotes(
  private val setting: Setting<PrePopulatedNotesInserted>,
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
      |Press is a cross-platform app for crafting notes, inspired by [Bear](bear.app) and written using Kotlin Multiplatform.
    """.trimMargin()
  )

  private val MARKDOWN_GUIDE = InsertNote(
      uuid = Uuid.parse("8f4192fd-38cf-4c15-8ac7-028d15fe5fc3")!!,
      content = """
      | # Markdown guide
      |Press understands standard markdown syntaxes, including: **bold**, *italic*, ~~strikethrough~~, and many more:
      |
      |### Code blocks
      |```
      |fun helloWorld() {
      |  println(""${'"'}
      |    Code blocks are wrapped inside 
      |    three ticks.
      |  ""${'"'})
      |}
      |``` 
      |
      |### Headings
      |Headings start with 1-6 `#` characters at the start of the line. 
      |
      |```
      |# Heading 1
      |## Heading 2
      |### Heading 3
      |#### Heading 4
      |##### Heading 5
      |###### Heading 6
      |```
      |
      |### Links
      |Links use a set of square brackets (`[]`) for describing the link text, followed by regular parentheses (`()`) containing the URL. 
      |
      |[Rick and Morty](https://www.imdb.com/title/tt2861424/).
      |
      |### Lists
      |Press supports ordered (numbered) and unordered (bulleted) lists. Unordered lists use asterisks, pluses, and hyphens — interchangeably — as list markers:
      |
      |- National Treasure
      |+ Ghost Rider
      |* Face/Off
      |
      |Ordered lists use numbers followed by periods:
      |
      |1. The Last of Us
      |2. Death Stranding
      |3. Cyberpunk 2077
      |
      |### Thematic breaks
      |Lines starting with three asterisks (`*`), hyphens (`-`) or underscores (`_`) are rendered as horizontal rules, a.k.a. “thematic breaks”. 
      |
      |---
      |
      |### Quotes
      |> A paragraph starting with a `>` are rendered as a quote. 
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
    val (inserted) = setting.get()

    if (inserted.not()) {
      repository.create(WELCOME, MARKDOWN_GUIDE, HOW_TO_CONTRIBUTE)
          .subscribeOn(ioScheduler)
          .subscribe {
            setting.set(PrePopulatedNotesInserted(true))
          }
    }
  }
}
