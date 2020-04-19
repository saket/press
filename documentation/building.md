## Building the projects

#### Android app
Press is primarily developed on [IntelliJ IDEA](https://www.jetbrains.com/idea/). If you're new to Android, importing the project on IntelliJ IDEA *should* walk you through all the steps needed for running the app. Please feel free to [ask](https://github.com/saket/press/issues/new) if that's not the case.  

#### macOS app
Press uses SwiftUI so you'll need macOS Catalina or higher. You can run the project by importing `native/mac` into Xcode through `native/mac/mac.xcworkspace`. 

In case you run into `Undefined symbols...` errors related to `sqlite`, you might have to manually enable linking of sqlite by adding `-lsqlite3` to your project's linker flags ([instructions](https://stackoverflow.com/questions/35313249/xcode-where-can-i-set-this-linker-flag-v)). In most cases, [SQLDelight](https://github.com/cashapp/sqldelight) should handle this for you automatically.
