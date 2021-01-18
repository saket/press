## Dependency Injection
Press uses [Koin](https://start.insert-koin.io/#/) for maintaining the object graph in shared code. These objects are wired to the Android app using [Dagger](https://dagger.dev/). Using Dagger in shared code would have been nice, but until we get codegen support on Kotlin Native, Press will be stuck with manually wiring dependencies using a service locator.

If you’re curious to see code, the Android and the shared DI graph are initialized in [PressApp.kt](https://github.com/saket/press/blob/4f050d2ed513118e51693f3295ca84a2bbf11ef6/androidApp/src/main/java/press/PressApp.kt#L33). 
