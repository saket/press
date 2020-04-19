<img width="100" height="100" src="resources/github/app_icon_github.png?raw=true"> 

<p align="center">
  <img width="285" src="resources/play_store/screenshots/home.png?raw=true">
  <img width="285" src="resources/play_store/screenshots/editor_new_note.png?raw=true">
  <img width="285" src="resources/play_store/screenshots/editor_existing_note.png?raw=true">
</p>

Press is a *wysiwyg* writer for crafting notes inspired by [Bear](https://bear.app). It uses markdown for styling and formatting text with a beautiful inline preview. 

Press was created as a proof-of-concept for exploring Kotlin Multiplatform, as well as the author’s frustration from the lack of minimal markdown note taking apps that work on all platforms, especially Android and macOS. If you relate to either of these reasons, Press is looking for contributors. 

- Android: https://play.google.com/store/apps/details?id=me.saket.press

- macOS: *work in progress*

- iOS: TODO

### Documentation
- [Building the projects](documentation/building.md)
- Architecture design records
  - [Overview of the UI architecture](documentation/architecture.md)
  - [Why Press uses custom Views for screens](documentation/screens_as_custom_views.md)
  - [Dependency injection with respect to shared code](documentation/dependency_injection.md)
  - [Testing and debugging shared code](documentation/testing.md)

### Contributing
Press is a barebones app right now and there are many improvements to make. The first steps would be setting up the iOS app, followed by macOS. Take a look at the [open issues](https://github.com/saket/Press/issues) and feel free to pick something up.

### License
```
Copyright 2019 Saket Narayan.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
