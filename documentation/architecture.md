## UI architecture
Press makes heavy usage of reactive code through-out the app. It uses [RxJava](https://github.com/ReactiveX/RxJava) on Android and [Reaktive](https://github.com/badoo/Reaktive) in shared code. 

Screens use a reactive MVI design for abstracting testable code. Each screen has one presenter that consumes user interactions in the form of “events”, performs logic with the help of data repositories and emits UI models back that can be rendered by the screen ([example](https://github.com/saket/press/blob/813dd203b62564f7d9b8994a151e14ef24fe9c64/shared/src/commonMain/kotlin/me/saket/press/shared/home/HomeUi.kt#L25)).

Here’s an sample from Android:

```kotlin
class HomeView : FrameLayout() {
  fun onAttachedToWindow() {
    composebutton.setOnClickListener {
      presenter.dispatch(NewNoteClicked)
    }

    presenter.models()
      .takeUntil(detachedFromWindow())
      .subscribe(::render)
  }
  
  private fun render(model: HomeModel) {
    ...
  }
}

class HomePresenter(...) {
  fun models(): Observable<HomeModel> {
    val models = database.noteQueries.allNotes()
      .asObservable(ioScheduler)
      .mapToList()
      .map { notes -> 
        HomeModel(notes)
      }
  
    return merge(
      models,
      viewEvents()
        .ofType<NewNoteClicked>
        .consumeOnNext { navigator.goTo(ComposeScreenKey) }
    )
  }
}
```

Take a look at [HomePresenter](https://github.com/saket/Press/blob/trunk/shared/src/commonMain/kotlin/me/saket/press/shared/home/HomePresenter.kt) or [EditorPresenter](https://github.com/saket/Press/blob/trunk/shared/src/commonMain/kotlin/me/saket/press/shared/editor/EditorPresenter.kt) to understand them in detail.
