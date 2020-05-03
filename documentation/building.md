## Building the projects

#### Android app
Press is primarily developed on [IntelliJ IDEA](https://www.jetbrains.com/idea/). If you're new to Android, importing the project on IntelliJ IDEA *should* walk you through all the steps needed for running the app. Please feel free to [ask](https://github.com/saket/press/issues/new) if that's not the case.  

#### macOS app
Press uses SwiftUI so you'll need macOS Catalina or higher. You can run the project by importing [`native/mac/mac.xcworkspace`](https://github.com/saket/press/tree/master/native/mac/mac.xcworkspace). 

##### Using AppCode
Although being the official IDE, Xcode isn't very good in comparison to IntelliJ's IDEs. If you share the same frustration or want a familiar environment setup when working with two platforms, using [AppCode](https://www.jetbrains.com/objc/) might be a good idea. Keep in mind of these gotchas:

1. AppCode doesn't re-generate Swift interfaces when shared code is updated. The project builds fine when new APIs are used, but autocomplete doesn't recognize them until a project restart. https://youtrack.jetbrains.com/issue/OC-20012. 

2. AppCode fails to resolve type parameter in a `ViewModifier`: https://youtrack.jetbrains.com/issue/OC-20058.

3. Swift Package Dependencies aren't supported, but support for it is supposed to be released soon. In the meantime, a workaround is mentioned here: https://youtrack.jetbrains.com/issue/OC-19012. Press doesn't use SPM so this not an issue _yet_. 
