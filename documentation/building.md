## Building the projects

#### Android app
Press is primarily developed on [IntelliJ IDEA](https://www.jetbrains.com/idea/). If you're new to Android, importing the project on IntelliJ IDEA *should* walk you through all the steps needed for running the app. Please feel free to [ask](https://github.com/saket/press/issues/new) if that's not the case.  

#### macOS app
Press uses SwiftUI so you'll need macOS Catalina or higher. You can run the project by importing [`native/mac/mac.xcworkspace`](https://github.com/saket/press/tree/trunk/native/mac/mac.xcworkspace). 

##### Using AppCode
Although being the official IDE, Xcode isn't very good in comparison to IntelliJ's IDEs. If you share the same frustration or want a familiar environment setup when working with two platforms, using [AppCode](https://www.jetbrains.com/objc/) might be a good idea. Keep in mind of these gotchas:

1. AppCode doesn't re-generate Swift interfaces when shared code is updated. The project builds fine when new APIs are used, but autocomplete doesn't recognize them until a project restart. https://youtrack.jetbrains.com/issue/OC-20012. 

2. AppCode fails to resolve type parameter in a `ViewModifier`: https://youtrack.jetbrains.com/issue/OC-20058.

3. Swift Package Dependencies aren't supported, but support for it is supposed to be released soon. In the meantime, a workaround is mentioned here: https://youtrack.jetbrains.com/issue/OC-19012. Press doesn't use SPM so this not an issue _yet_. 

#### API Keys
Press uses GitHub for syncing notes with a git repository. If you wish, you can [register](https://github.com/settings/applications/new) your own GitHub app and add its client secret key to `shared/secrets.properties`:

```groovy
github_client_id=...
github_client_secret=...
```

For ensuring sync is working as expected, Press runs its [tests](https://github.com/saket/press/blob/trunk/shared/src/commonTest/kotlin/me/saket/press/shared/sync/GitSyncerTest.kt) on a real repository. If the tests need to be run locally, you can specify your repository:

```groovy
git_test_repo_ssh_url=git@github.com:user/ExampleRepository.git
git_test_repo_branch=main
git_test_ssh_private_key=...
```
