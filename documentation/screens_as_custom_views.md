## Screens as custom Views
Press uses custom Views for designing screens instead of Fragments because Fragments are terrible and Views are great. They offer a simpler, synchronous lifecycle that is easy to use and carry none of the implicit gotchas that Fragments bring.

A single Activity is used for navigating between these Views. The navigation stack is built on top of [square/flow](https://github.com/square/flow) and can be seen [here](https://github.com/saket/press/tree/trunk/androidApp/src/main/java/press/navigation).

### Graduating from XML
Press uses custom Views are written in Kotlin code with minimal dependency on Xml because of multiple reasons:

**1. Runtime themes**

Press will have support for multiple themes soon. Unfortunately, changing themes on Android requires an Activity restart they cannot be modified on runtime either because they’re immutable. Forcing the user to restart the app would be terrible so Press keeps everything in code instead. Views listen to changes in themes and update at runtime ([example](https://github.com/saket/press/blob/f57ddf62349677c55bf65c7a8665b68331c8876d/androidApp/src/main/java/press/home/NoteRowView.kt#L41:L43)). 

**2. Contour**

The current state of Xml layouts leaves a lot to be desired. Tooling has been [bad](https://twitter.com/RunChristinaRun/status/1159147491738054656) for a while, the layout preview is good only for basic layouts and achieving dynamic layouts can become very verbose because of the static nature of Xml. Press embraces Kotlin and maths by using [Contour](https://github.com/cashapp/contour) for writing layouts. So far it has been a great experience and I’ve barely missed writing layouts in Xml.

The downside of the above two decisions is that Press has to fight the framework because it’s predominantly Xml-first. Want to change the cursor color in a text field at runtime? No way to do it. Press goes to [great lengths](https://github.com/saket/press/blob/trunk/androidApp/src/main/java/press/widgets/ThemeAwareCursorDrawable.kt) to find a work-around.
