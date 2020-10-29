## UI architecture
Press makes heavy usage of reactive code through-out the app. It uses [RxJava](https://github.com/ReactiveX/RxJava) on Android and [Reaktive](https://github.com/badoo/Reaktive) in shared code. 

Screens use a reactive MVI design for abstracting testable code. Each screen has one presenter that consumes user interactions in the form of “events”, performs logic with the help of data repositories and emits UI updates back that can be rendered by the screen. UI updates have two types:
1. UI content models, that describe the state of the layout ([example](https://github.com/saket/Press/blob/trunk/shared/src/commonMain/kotlin/me/saket/press/shared/home/HomeUi.kt#L10)).
2. UI effects, that cannot be modeled as state. For e.g., updating a text field once or navigating to a new screen ([example](https://github.com/saket/Press/blob/trunk/shared/src/commonMain/kotlin/me/saket/press/shared/home/HomeUi.kt#L20)).

Here’s an sample from Android:

```kotlin
class HomeScreen : FrameLayout() {
  fun onAttachedToWindow() {
    // View#clicks() comes from RxBinding.
    val uiEvents = noteList.clicks()
      .map { note -> NoteClicked(note) }

    presenter.uiUpdates(uiEvents)
      .takeUntil(detachedFromWindow())
      .subscribe(::render)
  }
}

class HomePresenter {
  fun uiUpdates(events: Observable<HomeEvent>) {
    return events
      .ofType<NoteClicked>
      .map { note -> OpenNote(note.uuid) }
  }
}
```

Take a look at [HomePresenter](https://github.com/saket/Press/blob/trunk/shared/src/commonMain/kotlin/me/saket/press/shared/home/HomePresenter.kt) or [EditorPresenter](https://github.com/saket/Press/blob/trunk/shared/src/commonMain/kotlin/me/saket/press/shared/editor/EditorPresenter.kt) to understand them in detail.
