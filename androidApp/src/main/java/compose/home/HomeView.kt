package compose.home

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import compose.ComposeApp
import io.reactivex.Observable
import me.saket.compose.shared.contentModels
import me.saket.compose.shared.home.HomeEvent
import me.saket.compose.shared.home.HomePresenter
import me.saket.compose.shared.home.HomeUiModel
import javax.inject.Inject

@SuppressLint("CheckResult")
class HomeView(context: Context) : ContourLayout(context) {

  @field:Inject
  lateinit var presenter: HomePresenter

//  private val notesList = RecyclerView(context).apply {
//    applyLayout(
//        x = leftTo { parent.left() }.rightTo { parent.right() },
//        y = topTo { parent.top() }.bottomTo { parent.bottom() }
//    )
//  }

  private val placeholderView = TextView(context).apply {
    text = "Loading…"
    textSize = 16f
    applyLayout(
        x = centerHorizontallyTo { parent.centerX() },
        y = centerVerticallyTo { parent.centerY() }
    )
  }

  init {
    ComposeApp.component.inject(this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    Observable.empty<HomeEvent>()
        .contentModels(presenter)
        .takeUntil(detaches())
        .subscribe(::render)
  }

  private fun render(model: HomeUiModel) {
    placeholderView.text = model.placeholder
  }
}