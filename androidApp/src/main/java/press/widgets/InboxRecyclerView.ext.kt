package press.widgets

import android.os.Looper
import io.reactivex.Observable
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.COLLAPSED
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.COLLAPSING
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.EXPANDED
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.EXPANDING
import me.saket.inboxrecyclerview.page.PageStateChangeCallbacks
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import press.extensions.suspendWhile

fun <T> Observable<T>.suspendWhileExpanded(page: ExpandablePageLayout): Observable<T> {
  return suspendWhile(page.stateChanges()) { page.isCollapsed.not() }
}

private val isMainThread: Boolean
  get() = Looper.myLooper() == Looper.getMainLooper()

internal fun ExpandablePageLayout.stateChanges(): Observable<PageState> {
  return Observable.create { emitter ->
    check(isMainThread) { "Not main thread: ${Thread.currentThread().name}" }

    val listener = object : PageStateChangeCallbacks {
      override fun onPageAboutToExpand(expandAnimDuration: Long) {
        emitter.onNext(EXPANDING)
      }

      override fun onPageExpanded() {
        emitter.onNext(EXPANDED)
      }

      override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
        emitter.onNext(COLLAPSING)
      }

      override fun onPageCollapsed() {
        emitter.onNext(COLLAPSED)
      }
    }

    emitter.onNext(currentState)
    addStateChangeCallbacks(listener)
    emitter.setCancellable { removeStateChangeCallbacks(listener) }
  }
}

