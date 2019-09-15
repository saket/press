package compose.widgets

import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.PageStateChangeCallbacks

fun ExpandablePageLayout.addStateChangeCallbacks(
  first: PageStateChangeCallbacks,
  vararg next: PageStateChangeCallbacks
) {
  addStateChangeCallbacks(first)
  next.forEach { addStateChangeCallbacks(it) }
}
