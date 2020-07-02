package me.saket.press.shared.note

import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.scheduler.Scheduler
import com.benasher44.uuid.uuidFrom
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.settings.Setting

data class PrePopulatedNotesInserted(val inserted: Boolean)

@Suppress("PrivatePropertyName")
class PrePopulatedNotes(
  private val setting: Setting<PrePopulatedNotesInserted>,
  private val repository: NoteRepository,
  private val schedulers: Schedulers
) {

  /**
   * @param uuid UUIDs are hardcoded to ensure they remain the same for everyone. It's important
   * that they get merged and not duplicated when notes are synced between multiple devices.
   */
  private val WELCOME = InsertNote(
      id = NoteId(uuidFrom("e731d56f-8db6-4351-a05e-8df27d5086f0")),
      content = """
      |# Welcome to Press
      |Press is a *wysiwyg* writer for crafting notes. It uses markdown for styling and formatting text with a beautiful inline preview. If you’re new to markdown, check out the next note for tips and tricks.
      |
      |Press is currently only available on Android as a proof-of-concept, but the plan is to introduce it to macOS and iOS in the near future using Kotlin Multiplatform. If you’d like to contribute, Press is hosted on Github:
      |
      |https://github.com/saket/press
      |
      |Kudos to [Bear](https://bear.app) for being the inspiration behind Press.
    """.trimMargin()
  )

  private val MARKDOWN_GUIDE = InsertNote(
      id = NoteId(uuidFrom("8f4192fd-38cf-4c15-8ac7-028d15fe5fc3")),
      content = """
      |# Markdown guide
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

  fun doWork() {
    val (inserted) = setting.get()!!

    if (inserted.not()) {
      repository.create(WELCOME, MARKDOWN_GUIDE)
          .subscribeOn(schedulers.io)
          .subscribe {
            setting.set(PrePopulatedNotesInserted(true))
          }
    }
  }
}
